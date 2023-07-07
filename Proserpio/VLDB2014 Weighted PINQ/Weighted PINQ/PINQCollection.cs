using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ
{
    // meant to hold global information used for a given Weighted PINQ program
    public static class PINQContext
    {
        public static List<Collection> inputs = new List<Collection>();

        public static void Start()
        {
            var parallelism = inputs.Max(x => x.Shards.Length);

            var inputTaskLists = new List<Task>[parallelism];
            for (int i = 0; i < inputTaskLists.Length; i++)
                inputTaskLists[i] = new List<Task>();

            for (int i = 0; i < PINQContext.inputs.Count; i++)
                for (int j = 0; j < inputs[i].Shards.Length; j++)
                    inputTaskLists[j].Add(inputs[i].Shards[j]);

            // could be done more directly with threading
            var tasks = Enumerable.Range(0, parallelism)
                                  .Select(i => System.Threading.Tasks.Task.Factory.StartNew(() => Task.Start(i, inputTaskLists[i])))
                                  .ToArray();

            System.Threading.Tasks.Task.WaitAll(tasks);
        }
    }

    public class PINQCollection<T> : Collection<Weighted<T>> where T : IEquatable<T>
    {
        #region Select, Where, SelectMany
        /// <summary>
        /// Record-to-record transformation
        /// </summary>
        /// <typeparam name="R">Result</typeparam>
        /// <param name="selector">Selection function</param>
        /// <returns>Collection resulting from application of selection function to each record.</returns>
        public PINQCollection<R> Select<R>(Func<T, R> selector) where R : IEquatable<R>
        {
            return new PINQCollection<R>(shards.Length).Initialize<T, T>(null, (i, c1) => new Operators.Select<T,R>(shards[i], c1, selector));
        }

        /// <summary>
        /// Filters records
        /// </summary>
        /// <param name="predicate">predicate</param>
        /// <returns>records satisfying predicate</returns>
        public PINQCollection<T> Where(Func<T, bool> predicate)
        {
            return new PINQCollection<T>(shards.Length).Initialize<T, T>(null, (i, c1) => new Operators.Where<T>(shards[i], c1, predicate));
        }

        // performs one-to-many transformation, normalized by the total weight in the result of selector
        public PINQCollection<R> SelectMany<R>(Func<T, IEnumerable<R>> selector) where R : IEquatable<R>
        {
            return new PINQCollection<R>(shards.Length).Initialize<T, T>(null, (i, c1) => new Operators.SelectMany<T, R>(shards[i], c1, selector));
        }
        #endregion

        #region Join
        /// <summary>
        /// Many-to-many matichng, normalized by the sums of weights associated with each key.
        /// </summary>
        /// <typeparam name="T2">Other source</typeparam>
        /// <typeparam name="K">Key</typeparam>
        /// <typeparam name="R">Result</typeparam>
        /// <param name="other">Other collection</param>
        /// <param name="key1">First key function</param>
        /// <param name="key2">Second key function</param>
        /// <param name="reducer">Reduction function</param>
        /// <returns>Reduction applied to each pair of matching input records</returns>
        public PINQCollection<R> Join<T2, K, R>(PINQCollection<T2> other, Func<T, K> key1, Func<T2, K> key2, Func<T, T2, R> reducer)
            where T2 : IEquatable<T2>
            where K : IEquatable<K>
            where R : IEquatable<R>
        {
            return new PINQCollection<R>(shards.Length).Initialize<T, T2, K>(key1, key2, (i, c1, c2) => new Operators.Join<T,T2,K,T,T2,R>(shards[i], c1, other.shards[i], c2, key1, key2, x => x, x => x, (k, x, y) => reducer(x,y)));
        }

        public PINQCollection<R> Join<T2, K, V1, V2, R>(PINQCollection<T2> other, Func<T, K> key1, Func<T2, K> key2, Func<T, V1> val1, Func<T2, V2> val2, Func<K, V1, V2, R> reducer)
            where T2 : IEquatable<T2>
            where V1 : IEquatable<V1>
            where V2 : IEquatable<V2>
            where K : IEquatable<K>
            where R : IEquatable<R>
        {
            return new PINQCollection<R>(shards.Length).Initialize<T, T2, K>(key1, key2, (i, c1, c2) => new Operators.Join<T, T2, K, V1, V2, R>(shards[i], c1, other.shards[i], c2, key1, key2, val1, val2, reducer));
        }
        #endregion

        #region Set operators

        /// <summary>
        /// returns the maximum of the weights in each collection
        /// </summary>
        /// <param name="other">Other collection</param>
        /// <returns>The maximum of weights of records in each collection</returns>
        public PINQCollection<T> Union(PINQCollection<T> other)
        {
            return new PINQCollection<T>(shards.Length).Initialize<T, T, T>(x => x, x => x, (i, c1, c2) => new Operators.Union<T>(shards[i], c1, other.shards[i], c2));
        }

        /// <summary>
        /// returns the minimum of the weights in each collection 
        /// </summary>
        /// <param name="other">Other collection</param>
        /// <returns>The minimum of weights of records in each collection</returns>
        public PINQCollection<T> Intersect(PINQCollection<T> other)
        {
            return new PINQCollection<T>(shards.Length).Initialize<T, T, T>(x => x, x => x, (i, c1, c2) => new Operators.Intersect<T>(shards[i], c1, other.shards[i], c2));
        }

        /// <summary>
        /// returns the sum of the weights in each collection
        /// </summary>
        /// <param name="other">Other collection</param>
        /// <returns>The sum of weights in each collection</returns>
        public PINQCollection<T> Concat(PINQCollection<T> other)
        {
            return new PINQCollection<T>(shards.Length).Initialize<T, T, T>(x => x, x => x, (i, c1, c2) => new Operators.Concat<T>(shards[i], c1, other.shards[i], c2));
        }

        /// <summary>
        /// returns the difference of weights in each collection
        /// </summary>
        /// <param name="other">Other collection</param>
        /// <returns>The difference of weights in the collections</returns>
        public PINQCollection<T> Except(PINQCollection<T> other)
        {
            return new PINQCollection<T>(shards.Length).Initialize<T, T, T>(x => x, x => x, (i, c1, c2) => new Operators.Except<T>(shards[i], c1, other.shards[i], c2));
        }

        #endregion

        #region Shave operators

        /// <summary>
        /// Slices each weighted record into fractional pieces.
        /// </summary>
        /// <typeparam name="R">Result type</typeparam>
        /// <param name="increment">fractional weight</param>
        /// <param name="reduction">reduces indexed results</param>
        /// <returns>Reducer applied to each fraction of input records.</returns>
        public PINQCollection<R> Shave<R>(double increment, Func<int, T, R> reduction) where R : IEquatable<R>
        {
            return new PINQCollection<R>(shards.Length).Initialize<T, T>(x => x, (i, c1) => new Operators.Shave<T, R>(shards[i], c1, (j,x) => increment, reduction));
        }

        /// <summary>
        /// Slices each weighted record into fractional pieces.
        /// </summary>
        /// <typeparam name="R">Result type</typeparam>
        /// <param name="increment">function producing the i'th fractional weight for record x</param>
        /// <param name="reduction">reduces indexed results</param>
        /// <returns>Reducer applied to each fraction of input records.</returns>
        public PINQCollection<R> Shave<R>(Func<int, T, double> increment, Func<int, T, R> reduction) where R : IEquatable<R>
        {
            return new PINQCollection<R>(shards.Length).Initialize<T, T>(x => x, (i, c1) => new Operators.Shave<T, R>(shards[i], c1, increment, reduction));
        }

        #endregion

        public PINQCollection<R> DPCount<K, R>(Func<T, K> key, Func<K, int, R> reduction) where R : IEquatable<R> where K : IEquatable<K>
        {
            return new PINQCollection<R>(shards.Length).Initialize<T, K>(key, (i, c1) => new Operators.DataParallelCount<K, T, R>(shards[i], c1, key, reduction));
        }

        public PINQCollection<R> GroupBy<K, V, R>(Func<T, K> key, Func<T, V> valueSelector, Func<K, ResizeableSubArray<V>, R> resultSelector) where R : IEquatable<R> where K : IEquatable<K> where V : IEquatable<V>
        {
            return new PINQCollection<R>(shards.Length).Initialize<T, K>(key, (i, c1) => new Operators.GroupBy<T, K, V, R>(shards[i], c1, key, valueSelector, resultSelector));
        }

        /// <summary>
        /// Counts records by key, with Laplace noise.
        /// </summary>
        /// <typeparam name="K">Key</typeparam>
        /// <param name="keyFunction">key function</param>
        /// <param name="epsilon">level of differential privacy</param>
        /// <returns>Collection of noised counts indexed by key</returns>
        public Operators.CountCollection<K,T> Count<K>(Func<T,K> keyFunction, double epsilon)
        {
            var temp = new Operators.CountCollection<K,T>(shards.Length);

            temp.Initialize<T, K>(keyFunction, (i, c1) => new Operators.Count<K, T>(shards[i], c1, keyFunction, epsilon));

            return temp;
        }

        /// <summary>
        /// Monitors a collection, for debugging purposes
        /// </summary>
        /// <param name="prefix">string to prefix results with</param>
        /// <returns>Input</returns>
        public PINQCollection<T> Monitor(string prefix)
        {
            return new PINQCollection<T>(shards.Length).Initialize<T, T>(x => x, (i, c1) => new Operators.Monitor<T>(shards[i], c1, prefix));
        }

        /// <summary>
        /// Ingress point for dataflow computation.
        /// </summary>
        /// <param name="generator">Data generator</param>
        /// <param name="parallelism">Degree of parallelism</param>
        /// <returns>An input collection rooting dataflow computation</returns>
        public static Operators.InputCollection<T> Input(Func<int, IEnumerable<Weighted<T>>> generator, int parallelism)
        {
            var temp = new Operators.InputCollection<T>(parallelism);

            temp.Initialize<T, T>(x => x, (i, c1) => new Operators.Input<T>(generator(i)));

            PINQContext.inputs.Add(temp);

            return temp;
        }

        /// <summary>
        /// Ingress point for dataflow computation
        /// </summary>
        /// <param name="parallelism">Degree of parallelism</param>
        /// <returns>An input collection rooting dataflow computation</returns>
        public static Operators.InputCollection<T> Input(int parallelism)
        {
            return Input(i => Enumerable.Empty<Weighted<T>>(), parallelism);
        }

        public PINQCollection<T> Initialize<S1, K>(Func<S1, K> key, Func<int, Channel<Weighted<S1>>, CollectionShard<Weighted<T>>> initializer) where S1 : IEquatable<S1>
        {
            base.Initialize<Weighted<S1>, K>(key == null ? (Func<Weighted<S1>,K>) null : x => key(x.record), initializer);
            
            return this;
        }

        public PINQCollection<T> Initialize<S1, S2, K>(Func<S1, K> key1, Func<S2, K> key2, Func<int, Channel<Weighted<S1>>,Channel<Weighted<S2>>, CollectionShard<Weighted<T>>> initializer) 
            where S1 : IEquatable<S1>
            where S2 : IEquatable<S2>
        {
            base.Initialize<Weighted<S1>, Weighted<S2>, K>(x => key1(x.record), x => key2(x.record), initializer);
            return this;
        }

        public PINQCollection(int parallelism) : base(parallelism) { }
    }
}
