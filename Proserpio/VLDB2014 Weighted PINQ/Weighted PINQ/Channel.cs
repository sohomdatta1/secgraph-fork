using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using System.Collections.Concurrent;

namespace Microsoft.Research.WeightedPINQ
{
    public enum MessageStatus
    {
        Normal = 0,         // indicates normal status; more messages on the way
        FinalNonEmpty = 1,  // indicates last message; some data sent to someone
        FinalEmpty = 2      // indicates last message; no data sent to anyone
    }

    public struct Message<T>
    {
        public T[] payload;
        public int length;
        public MessageStatus status;

        public bool Final { get { return status == MessageStatus.FinalEmpty || status == MessageStatus.FinalNonEmpty; } }

        public Message(BufferPool<T> pool)
        {
            payload = pool != null ? pool.CheckOut(1024) : new T[1024];

            length = 0;
            status = MessageStatus.Normal;
        }
    }

    // the channel acts as the data exchange between otherwise independent data-parallel computations.
    // there is a communication protocol used to allow cut-through, but ensure each knows when it is time to stop.
    // transmissions take the form of arrays of data (possibly zero-length) or null. 
    // null indicates that no data was sent to anyone.

    public interface SendChannel<T>
    {
        void Send(ref Message<T> message);
    }

    public interface RecvChannel<T>
    {
        void Recv(ref Message<T> message);
    }

    public interface Channel<S, T> : SendChannel<S>, RecvChannel<T> { }

    public interface Channel<T> : Channel<T, T> { }

    public class ExchangeChannel<T> : Channel<T>
    {
        BlockingCollection<Message<T>>[] queues;    // these queues are how the various shards of the channel share messages and data
        int index;                                  // each channel object is a shard of the logical channel. index of queues.Length

        Func<T, int> keyFunction;                   // function indicating which shard each record should be routed to. usually just the hash of a key.

        bool emptySend = true;
        Message<T>[] outgoing; // keeps partially assembled messages destined for other queues.

        // each call to send introduces some number of records of type T into the channel.
        // the final batch should end with a Final status, of either type (it should be a bug to mis-specify the status).
        // the channel is in charge of doing the data exchange, alerting other channels to their shard of communication,
        // with the correct status applied where appropriate.
        public void Send(ref Message<T> message)
        {
            if (keyFunction == null)
                return;

            // Console.WriteLine("Send invoked: {0}\t{1} {2}", index, message.length, message.status);

            if (message.length > 0)
                emptySend = false;

            for (int i = 0; i < message.length; i++)
            {
                var record = message.payload[i];

                // determine which of the queues the transmission is destined for
                var target = keyFunction(record) % queues.Length;

                // we may need to free up some space by sending a transmission
                if (outgoing[target].length == outgoing[target].payload.Length)
                {
                    queues[target].Add(outgoing[target]);       // add outgoing tranmission to queue
                    outgoing[target] = new Message<T>(ThreadLocalBufferPools<T>.pool.Value);    // may need to initialize payload
                }

                // copy the object to the appropriate outgoing message, and bump the count
                outgoing[target].payload[outgoing[target].length++] = record;
            }

            // if this was the last message, we should finish all of the outgoing queues.
            if (message.Final)
            {
                for (int i = 0; i < outgoing.Length; i++)
                {
                    outgoing[i].status = emptySend ? MessageStatus.FinalEmpty : MessageStatus.FinalNonEmpty;

                    queues[i].Add(outgoing[i]);
                    outgoing[i] = new Message<T>(ThreadLocalBufferPools<T>.pool.Value);
                }

                emptySend = true;
            }

            // do something with message, or just leave it alone for now.
            // probably best to leave it, so that multiple sends don't copy data.
        }

        int epochEnds = 0;
        bool emptyRecv = true;

        // each call to Recv updates the message parameter, and gives the appearance of a single producer/consumer channel.
        // that is, each phase of communication has some amount of data with normal status, and a final message with either empty or non-empty status.
        // the channel is meant to be smart and hide the number of other threads from the consumer of the messages.
        public void Recv(ref Message<T> message)
        {
            message.length = 0;     // this is our indication that we are unsatisfied with this data.

            // continue while no valid data at hand, but some is expected.
            while (message.length == 0 && (epochEnds % queues.Length > 0 || queues[index].Count > 0))
            {
                // fetch the next message
                ThreadLocalBufferPools<T>.pool.Value.CheckIn(message.payload);
                message = queues[index].Take();

                // the message may indicate completion of communication. good to note.
                if (message.Final)
                    epochEnds++;

                if (message.status != MessageStatus.FinalEmpty)
                    emptyRecv = false;
            }

            // we have reach the end of an epoch
            if (epochEnds % queues.Length == 0 && queues[index].Count == 0)
            {
                // we should return a message indicating the end of the epoch
                message.status = emptyRecv ? MessageStatus.FinalEmpty : MessageStatus.FinalNonEmpty;

                emptyRecv = true;   // as the channel is drained, reset our view of its contents

                // Console.WriteLine("Index {0}; poolsize {1}", index, ThreadLocalBufferPools<T>.pool.Value.Count);
            }
            else
            {
                message.status = MessageStatus.Normal;
            }
        }

        public ExchangeChannel(int id, Func<T, int> k, BlockingCollection<Message<T>>[] qs)
        {
            index = id;
            keyFunction = k;
            queues = qs;

            outgoing = new Message<T>[queues.Length];
            for (int i = 0; i < outgoing.Length; i++)
                outgoing[i] = new Message<T>(ThreadLocalBufferPools<T>.pool.Value);
        }

        public static Channel<T>[] Allocate(int n, Func<T, int> key)
        {
            // assemble the first set of blocking collections and channels
            var queues = new BlockingCollection<Message<T>>[n];
            for (int i = 0; i < queues.Length; i++)
                queues[i] = new BlockingCollection<Message<T>>();

            var channels = new ExchangeChannel<T>[n];
            for (int i = 0; i < channels.Length; i++)
                channels[i] = new ExchangeChannel<T>(i, key, queues);

            return channels;
        }
    }
}
