using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ.Operators
{
    class DataParallelCount<K,T,R> : UnaryCollectionShard<Weighted<T>, K, int, Weighted<R>> 
        where T : IEquatable<T>
        where R : IEquatable<R>
    {
        //for a particular key and count what I should output as the record
        Func<K, int, R> reducer;

        public override IEnumerable<Weighted<R>> Contents()
        {
            foreach (var state in states)
                yield return new Weighted<R>(reducer(state.Key, state.Value), Int32.MaxValue / 2);
        }

        protected override int UpdateState(K key, int state, int update)
        {
            int delta = 0;

            for (var index = update; index >= 0; index = updateChain[index].previous)
                delta += (int) (updateChain[index].update.weight / Int32.MaxValue);

            if (delta != 0 && state != 0)
                Send(new Weighted<R>(reducer(key, state), -Int32.MaxValue / 2));

            state += delta;
            if (delta != 0 && state != 0)
                Send(new Weighted<R>(reducer(key, state), Int32.MaxValue / 2));

            return state;
        }

        public DataParallelCount(CollectionShard<Weighted<T>> source1, Channel<Weighted<T>> channel1, Func<T,K> key, Func<K, int, R> r)
            : base("Count", source1, channel1, x => key(x.record))
        {
            reducer = r;
        }
    }
}
