using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ.Operators
{
    class Concat<T> : BinaryCollectionShard<Weighted<T>, Weighted<T>, Weighted<T>> where T : IEquatable<T>
    {
        CollectionShard<Weighted<T>> source1;
        CollectionShard<Weighted<T>> source2;

        public override void Subscribe(SendChannel<Weighted<T>> channel, Task task)
        {
            source1.Subscribe(channel, task);
            source2.Subscribe(channel, task);
        }

        public Concat(CollectionShard<Weighted<T>> s1, Channel<Weighted<T>> channel1, CollectionShard<Weighted<T>> s2, Channel<Weighted<T>> channel2)
            : base("Concat", null, null, null, null)
        {
            source1 = s1;
            source2 = s2;
        }
    }
}
