using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ.Operators
{
    public class NegationChannel<T> : SendChannel<Weighted<T>> where T : IEquatable<T>
    {
        SendChannel<Weighted<T>> baseChannel;

        public void Send(ref Message<Weighted<T>> message)
        {
            // tamper with the message payload
            for (int i = 0; i < message.length; i++)
                message.payload[i].weight *= -1;

            baseChannel.Send(ref message);

            // un-tamper with the message payload
            for (int i = 0; i < message.length; i++)
                message.payload[i].weight *= -1;
        }

        public NegationChannel(SendChannel<Weighted<T>> channel) { baseChannel = channel; }
    }

    public class Except<T> : BinaryCollectionShard<Weighted<T>, Weighted<T>, Weighted<T>> where T : IEquatable<T>
    {
        CollectionShard<Weighted<T>> source1;
        CollectionShard<Weighted<T>> source2;

        public override void Subscribe(SendChannel<Weighted<T>> channel, Task task)
        {
            source1.Subscribe(channel, task);

            var negation = new NegationChannel<T>(channel);

            source2.Subscribe(negation, task);
        }

        public Except(CollectionShard<Weighted<T>> s1, Channel<Weighted<T>> channel1, CollectionShard<Weighted<T>> s2, Channel<Weighted<T>> channel2)
            : base("Except", null, null, null, null)
        {
            source1 = s1;
            source2 = s2;
        }
    }
}
