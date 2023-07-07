using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using System.Collections.Concurrent;

namespace Microsoft.Research.WeightedPINQ
{
    public interface Collection
    {
        Task[] Shards { get; }
    }

    // representation of a data-parallel private collection
    // comprises multiple shards of data, usually by key
    public class Collection<T> : Collection
    {
        public CollectionShard<T>[] shards;

        public Task[] Shards { get { return shards; }}

        public void Start()
        {
            Task.Start(shards);
        }

        // used to initialize a collection with a single channel input, of type S1
        public Collection<T> Initialize<S1, K>(Func<S1, K> key1, Func<int, Channel<S1>, CollectionShard<T>> initializer)
        {
            var parts = shards.Length;
            // var offset = new Random().Next(parts);  // attempting to avoid bad correlation across multiple joins (broken?)

            var channels1 = ExchangeChannel<S1>.Allocate(shards.Length, key1 == null ? (Func<S1, int>)null : x => (Int32.MaxValue & key1(x).GetHashCode()) % parts);

            // concurrently initializes and starts the shards, to warm up their input.
            Task.Start(shards.Length, i => shards[i] = initializer(i, channels1[i]));
            Task.Start(shards);

            return this;
        }

        // used to initialize a collection with two input channels, of types S1 and S2
        public Collection<T> Initialize<S1, S2, K>(Func<S1, K> key1, Func<S2, K> key2, Func<int, Channel<S1>, Channel<S2>, CollectionShard<T>> initializer)
        {
            var parts = shards.Length;
            var offset = new Random(0).Next(parts);  // attempting to avoid bad correlation across multiple joins

            var channels1 = ExchangeChannel<S1>.Allocate(shards.Length, key1 == null ? (Func<S1, int>) null : x => (Int32.MaxValue & (offset + key1(x).GetHashCode())) % parts);
            var channels2 = ExchangeChannel<S2>.Allocate(shards.Length, key2 == null ? (Func<S2, int>) null : x => (Int32.MaxValue & (offset + key2(x).GetHashCode())) % parts);

            // concurrently initializes and starts the shards, to warm up their input.
            Task.Start(shards.Length, i => shards[i] = initializer(i, channels1[i], channels2[i]));
            Task.Start(shards);

            return this;
        }

        // we can't do much until we know more about the types involved
        public Collection(int parallelism)
        {
            shards = new CollectionShard<T>[parallelism];
        }
    }
}
