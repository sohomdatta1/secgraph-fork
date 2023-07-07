using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ
{
    public class CollectionShard<T> : Task
    {
        public SendChannel<T>[] outputChannels = new SendChannel<T>[] { };
        
        public Task[] dependents = new Task[] {};

//subscribe to a collection shard (pull)
        public virtual void Subscribe(SendChannel<T> channel, Task task)
        {
            //dependes - list people the need to run next
            dependents = dependents.Concat(new Task[] { task }).ToArray();
            //where to send to stuff
            outputChannels = outputChannels.Concat(new SendChannel<T>[] { channel }).ToArray();

            
            var tempMessage = new Message<T>(null);
            //ask current content for that shard
            //contentes - virtual method each one has to define to create the diff.
            foreach (var record in Contents())
            {
                //Console.WriteLine("Sending {0}", record);

                //if all the message is ready to send - fill up all the message in payload
                if (tempMessage.length == tempMessage.payload.Length)
                {
                    channel.Send(ref tempMessage);

                    tempMessage.length = 0;
                }

                tempMessage.payload[tempMessage.length++] = record;
            }
            //send even if the payload is not full...
            tempMessage.status = MessageStatus.FinalNonEmpty;
            //send to every single channel
            channel.Send(ref tempMessage);
        }

        public virtual IEnumerable<T> Contents()
        {
            Console.WriteLine("In default Contents()");
            yield break;
        }

        Message<T> message = new Message<T>(null);
        public void Send(T record)
        {
            if (message.length == message.payload.Length)
            {
                message.status = MessageStatus.Normal;
                for (int i = 0; i < outputChannels.Length; i++)
                    outputChannels[i].Send(ref message);

                message.length = 0;
            }

            message.payload[message.length++] = record;
        }

        public void DoneSending()
        {
            message.status = MessageStatus.FinalEmpty;

            // signals the end of an epoch by sending null
            for (int i = 0; i < outputChannels.Length; i++)
                outputChannels[i].Send(ref message);

            message.length = 0;
        }

        protected virtual void Compute()
        {
        }

        public CollectionShard(string name) : base(name) { }
    }

    public class UnaryCollectionShard<T1, R> : CollectionShard<R>
    {
        //imput channel
        RecvChannel<T1> input1;
        //output channel
        public virtual void OnInput1(T1 record) { }

        Message<T1> message1 = new Message<T1>(null);

        public override Task[] Work()
        {
            //Console.WriteLine("{0} working", this);

            //if input channel is not empty
            if (input1 != null)
            {
                do
                {
                    input1.Recv(ref message1);
                    for (int i = 0; i < message1.length; i++)
                        OnInput1(message1.payload[i]);
                }
                while (!message1.Final);
            }

            // if the input channel was not empty, schedule dependents.
            if (message1.status == MessageStatus.FinalNonEmpty)
            {
                Compute();
                //basically like flush
                DoneSending();
                //additional tasks we have to do, possible nothing
                return dependents;
            }
            else
            {
                return null;
            }
        }

        public UnaryCollectionShard(string name, CollectionShard<T1> source1, Channel<T1> channel1)
            : base(name)
        {
            input1 = channel1;

            if (source1 != null) source1.Subscribe(channel1, this);
        }
    }

    public class BinaryCollectionShard<T1, T2, R> : CollectionShard<R>
    {
        RecvChannel<T1> input1;
        RecvChannel<T2> input2;

        public virtual void OnInput1(T1 record) { }
        public virtual void OnInput2(T2 record) { }

        Message<T1> message1 = new Message<T1>(null);
        Message<T2> message2 = new Message<T2>(null);

        public override Task[] Work()
        {
            //Console.WriteLine("{0} working", this);

            if (input1 != null)
            {
                do
                {
                    input1.Recv(ref message1);
                    for (int i = 0; i < message1.length; i++)
                        OnInput1(message1.payload[i]);
                }
                while (!message1.Final);
            }

            if (input2 != null)
            {
                do
                {
                    input2.Recv(ref message2);
                    for (int i = 0; i < message2.length; i++)
                        OnInput2(message2.payload[i]);
                }
                while (!message2.Final);
            }

            // if the input channel was not empty, schedule dependents.
            if (message1.status == MessageStatus.FinalNonEmpty || message2.status == MessageStatus.FinalNonEmpty)
            {
                Compute();

                DoneSending();

                return dependents;
            }
            else
                return null;
        }

        public BinaryCollectionShard(string name, CollectionShard<T1> source1, Channel<T1> channel1, CollectionShard<T2> source2, Channel<T2> channel2)
            : base(name)
        {
            input1 = channel1;
            input2 = channel2;

            if (source1 != null) source1.Subscribe(channel1, this);
            if (source2 != null) source2.Subscribe(channel2, this);
        }
    }

    public class UnaryCollectionShard<T1, K, S, R> : UnaryCollectionShard<T1, R> where S : IEquatable<S>
    {
        Func<T1, K> keyFunction1;

        protected struct UpdateChain1
        {
            public int previous;
            public T1 update;

            public UpdateChain1(int p, T1 u) { previous = p; update = u; }
            public UpdateChain1(T1 u) { previous = -1; update = u; }
        }

        protected Dictionary<K, S> states = new Dictionary<K, S>();
        protected Dictionary<K, int> stateUpdates = new Dictionary<K, int>();
        
        protected List<K> toUpdate = new List<K>();

        protected List<UpdateChain1> updateChain = new List<UpdateChain1>();

        public override void OnInput1(T1 input1)
        {
            var key = keyFunction1(input1);

            int stateUpdate;
            if (!stateUpdates.TryGetValue(key, out stateUpdate))
                stateUpdate = -1;
           
            updateChain.Add(new UpdateChain1(stateUpdate, input1));

            if (stateUpdate == -1)
                toUpdate.Add(key);
            
            stateUpdates[key] = updateChain.Count - 1;
        }

        protected virtual S UpdateState(K key, S state, int updateRootIndex) { return state; }

        protected override void Compute()
        {
            for (int i = 0; i < toUpdate.Count; i++)
            {
                var key = toUpdate[i];

                int updateRootIndex;
                if (stateUpdates.TryGetValue(key, out updateRootIndex))
                {
                    var state = default(S);
                    if (!states.TryGetValue(key, out state))
                        state = default(S);

                    var newState = UpdateState(key, state, updateRootIndex);

                    if (newState.Equals(default(S)))
                        states.Remove(key);
                    else
                        states[key] = newState;

                    stateUpdates.Remove(key);
                }
            }

            updateChain.Clear();
            toUpdate.Clear();
        }
        
        public UnaryCollectionShard(string name, CollectionShard<T1> source1, Channel<T1> channel1, Func<T1, K> keyFunc1)
            : base(name, source1, channel1)
        {
            keyFunction1 = keyFunc1;
        }
    }

    public class BinaryCollectionShard<T1, T2, K, V1, V2, S, R> : BinaryCollectionShard<T1, T2, R> 
        where S : IEquatable<S>
    {
        Func<T1, K> keyFunction1;
        Func<T2, K> keyFunction2;

        Func<T1, V1> valueSelector1;
        Func<T2, V2> valueSelector2;

        protected struct UpdateChain1
        {
            public int previous;
            public V1 update;

            public UpdateChain1(int p, V1 u) { previous = p; update = u; }
            public UpdateChain1(V1 u) { previous = -1; update = u; }
        }

        protected struct UpdateChain2
        {
            public int previous;
            public V2 update;

            public UpdateChain2(int p, V2 u) { previous = p; update = u; }
            public UpdateChain2(V2 u) { previous = -1; update = u; }
        }

        protected Dictionary<K, S> states = new Dictionary<K, S>();
        protected Dictionary<K, int> stateUpdates1 = new Dictionary<K, int>();
        protected Dictionary<K, int> stateUpdates2 = new Dictionary<K, int>();

        protected List<K> toUpdate = new List<K>();

        protected List<UpdateChain1> updateChain1 = new List<UpdateChain1>();
        protected List<UpdateChain2> updateChain2 = new List<UpdateChain2>();

        public override void OnInput1(T1 input1)
        {
            var key = keyFunction1(input1);

            int stateUpdate;
            if (!stateUpdates1.TryGetValue(key, out stateUpdate))
                stateUpdate = -1;

            updateChain1.Add(new UpdateChain1(stateUpdate, valueSelector1(input1)));

            if (stateUpdate == -1)
                toUpdate.Add(key);

            stateUpdates1[key] = updateChain1.Count - 1;
        }

        public override void OnInput2(T2 input2)
        {
            var key = keyFunction2(input2);

            int stateUpdate;
            if (!stateUpdates2.TryGetValue(key, out stateUpdate))
                stateUpdate = -1;

            updateChain2.Add(new UpdateChain2(stateUpdate, valueSelector2(input2)));

            if (stateUpdate == -1)
                toUpdate.Add(key);

            stateUpdates2[key] = updateChain2.Count - 1;
        }

        protected virtual S UpdateState(K key, S state, int updateRootIndex1, int updateRootIndex2) { return state; }

        protected override void Compute()
        {
            for (int i = 0; i < toUpdate.Count; i++)
            {
                var key = toUpdate[i];

                int updateRootIndex1;
                if (!stateUpdates1.TryGetValue(key, out updateRootIndex1))
                    updateRootIndex1 = -1;
                
                int updateRootIndex2;
                if (!stateUpdates2.TryGetValue(key, out updateRootIndex2))
                    updateRootIndex2 = -1;

                if (updateRootIndex1 >= 0 || updateRootIndex2 >= 0)
                {
                    var state = default(S);
                    if (!states.TryGetValue(key, out state))
                        state = default(S);

                    var newState = UpdateState(key, state, updateRootIndex1, updateRootIndex2);

                    if (!newState.Equals(default(S)))
                        states[key] = newState;
                    else
                        states.Remove(key);

                    if (updateRootIndex1 >= 0)
                        stateUpdates1.Remove(key);
                    if (updateRootIndex2 >= 0)
                        stateUpdates2.Remove(key);
                }
            }

            updateChain1.Clear();
            updateChain2.Clear();
            toUpdate.Clear();
        }

        public BinaryCollectionShard(string name, 
                                     CollectionShard<T1> source1, Channel<T1> channel1, Func<T1, K> keyFunc1, Func<T1, V1> valFunc1, 
                                     CollectionShard<T2> source2, Channel<T2> channel2, Func<T2, K> keyFunc2, Func<T2, V2> valFunc2)
            : base(name, source1, channel1, source2, channel2)
        {
            keyFunction1 = keyFunc1;
            keyFunction2 = keyFunc2;

            valueSelector1 = valFunc1;
            valueSelector2 = valFunc2;
        }
    }

    public class BinaryCollectionShard<T1, T2, K, S, R> : BinaryCollectionShard<T1, T2, K, T1, T2, S, R> where S : IEquatable<S>
    {
        public BinaryCollectionShard(string name,
                                 CollectionShard<T1> source1, Channel<T1> channel1, Func<T1, K> keyFunc1,
                                 CollectionShard<T2> source2, Channel<T2> channel2, Func<T2, K> keyFunc2)
            : base(name, source1, channel1, keyFunc1, x => x, source2, channel2, keyFunc2, x => x)
        {
        }

    }
}
