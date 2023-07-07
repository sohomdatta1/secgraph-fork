using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ.Operators
{
    public class WhereChannel<T> : SendChannel<Weighted<T>> where T : IEquatable<T>
    {
        Func<T, bool> predicate;
        SendChannel<Weighted<T>> baseChannel;

        Message<Weighted<T>> newMessage = new Message<Weighted<T>>(null);

        public void Send(ref Message<Weighted<T>> message)
        {
            newMessage.status = message.status;
            for (int i = 0; i < message.length; i++)
                if (predicate(message.payload[i].record))
                    newMessage.payload[newMessage.length++] = message.payload[i];

            baseChannel.Send(ref newMessage);

            newMessage.length = 0;
        }

        public WhereChannel(SendChannel<Weighted<T>> channel, Func<T, bool> p) { baseChannel = channel; predicate = p; }
    }

    public class Where<T> : UnaryCollectionShard<Weighted<T>, Weighted<T>> where T : IEquatable<T>
    {
        CollectionShard<Weighted<T>> source1;
        Func<T, bool> predicate;
    
        public override void Subscribe(SendChannel<Weighted<T>> channel, Task task)
        {
            var whereChannel = new WhereChannel<T>(channel, predicate);

            source1.Subscribe(whereChannel, task);
        }

        public Where(CollectionShard<Weighted<T>> s1, Channel<Weighted<T>> channel1, Func<T,bool> p)
            : base("Where", null, null)
        {
            source1 = s1;
            predicate = p;
        }
    }
}
