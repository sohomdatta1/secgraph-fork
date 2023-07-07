using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ.Operators
{
    public class Monitor<T> : UnaryCollectionShard<Weighted<T>, Weighted<T>>
        where T : IEquatable<T>
    {
        CollectionShard<Weighted<T>> source;
        string prefix;

        public override void OnInput1(Weighted<T> record)
        {
            Console.WriteLine("{1}{0}", record, prefix);
        }

        public override IEnumerable<Weighted<T>> Contents()
        {
            return source.Contents(); 
        }

        public Monitor(CollectionShard<Weighted<T>> source1, Channel<Weighted<T>> channel1, string p)
            : base("Monitor", source1, channel1)
        {
            source = source1;
            prefix = p;
        }
    }
}
