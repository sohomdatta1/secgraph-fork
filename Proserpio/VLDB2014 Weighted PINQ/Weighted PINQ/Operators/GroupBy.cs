using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ.Operators
{
    public struct GroupByState<V> : IEquatable<GroupByState<V>>
        where V : IEquatable<V>
    {
        public ResizeableSubArray<Weighted<V>> elements;

        public bool Equals(GroupByState<V> that)
        {
            if (this.elements.Count != that.elements.Count)
                return false;

            for (int i = 0; i < elements.Count; i++)
                if (!elements.Array[i].Equals(that.elements.Array[i]))
                    return false;

            return true;
        }
    }

    public class GroupBy<T, K, V, R> : UnaryCollectionShard<Weighted<T>, K, GroupByState<V>, Weighted<R>>
        where T : IEquatable<T>
        where V : IEquatable<V>
        where K : IEquatable<K>
        where R : IEquatable<R>
    {
        Func<T, V> selector;
        Func<K, ResizeableSubArray<V>, R> resultSelector;

        Dictionary<V, Int64> updateAccumulator = new Dictionary<V, Int64>();
        Dictionary<R, Int64> resultAccumulator = new Dictionary<R, Int64>();

        ResizeableSubArray<V> groupContent = new ResizeableSubArray<V>();

        protected override GroupByState<V> UpdateState(K key, GroupByState<V> state, int updateRootIndex)
        {
            if (state.elements.Array == null) 
                state.elements = new ResizeableSubArray<Weighted<V>>(0);

            updateAccumulator.Clear();
            resultAccumulator.Clear();
            
            // first determine and subtract the previous outputs
            groupContent.Clear();
            for (int i = 0; i < state.elements.Count; i++)
            {
                // set the weight associated with this record
                updateAccumulator[state.elements.Array[i].record] = state.elements.Array[i].weight;

                // add the element to the materialized list
                groupContent.Add(state.elements.Array[i].record);

                var weight = (state.elements.Array[i].weight - (i == state.elements.Count - 1 ? 0 : state.elements.Array[i + 1].weight)) / 2;
                if (weight != 0)
                {
                    var result = resultSelector(key, groupContent);

                    if (!resultAccumulator.ContainsKey(result))
                        resultAccumulator.Add(result, 0);

                    resultAccumulator[result] -= weight;
                }
            }

            // update the accumulation based on the updates we see.
            for (int index = updateRootIndex; index >= 0; index = updateChain[index].previous)
            {
                var selected = selector(updateChain[index].update.record);
                if (!updateAccumulator.ContainsKey(selected))
                    updateAccumulator.Add(selected, 0);

                updateAccumulator[selected] += updateChain[index].update.weight;
            }

            // load the new accumulation into state
            state.elements.Clear();
            foreach (var pair in updateAccumulator.OrderByDescending(x => x.Value))
            {
                if (pair.Value > 0)
                    state.elements.Add(new Weighted<V>(pair.Key, pair.Value));
                if (pair.Value < 0)
                    Console.Error.WriteLine("Negative accumulation in GroupBy; probably a bug somewhere");
            }

            // second determine and add the current outputs
            groupContent.Clear();
            for (int i = 0; i < state.elements.Count; i++)
            {
                // add the element to the materialized list
                groupContent.Add(state.elements.Array[i].record);
                var weight = (state.elements.Array[i].weight - (i == state.elements.Count - 1 ? 0 : state.elements.Array[i + 1].weight)) / 2;
                if (weight != 0)
                {
                    var result = resultSelector(key, groupContent);

                    if (!resultAccumulator.ContainsKey(result))
                        resultAccumulator.Add(result, 0);

                    // subtract half the difference to the next weight, with zero for the last weight
                    resultAccumulator[result] += weight;
                }
            }

            foreach (var pair in resultAccumulator)
                if (pair.Value != 0)
                    Send(new Weighted<R>(pair.Key, pair.Value));

            return state;
        }

        public override IEnumerable<Weighted<R>> Contents()
        {
            foreach (var pair in states)
            {
                var key = pair.Key;
                var state = pair.Value;

                groupContent.Clear();
                for (int i = 0; i < state.elements.Count; i++)
                {
                    // add the element to the materialized list
                    groupContent.Add(state.elements.Array[i].record);

                    var result = resultSelector(key, groupContent);

                    var weight = (state.elements.Array[i].weight - (i == state.elements.Count - 1 ? 0 : state.elements.Array[i + 1].weight)) / 2;

                    if (weight != 0)
                        yield return new Weighted<R>(result, weight);
                }
            }
        }

        public GroupBy(CollectionShard<Weighted<T>> source, Channel<Weighted<T>> channel,
                    Func<T, K> keyFunc, 
                    Func<T, V> valFunc,
                    Func<K, ResizeableSubArray<V>, R> resultSel)
            : base("GroupBy", source, channel, x => keyFunc(x.record))
        {
            selector = valFunc;
            resultSelector = resultSel;
        }
    }
}
