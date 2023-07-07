using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ.Operators
{
    public struct JoinState<V1, V2> : IEquatable<JoinState<V1, V2>>
        where V1 : IEquatable<V1> 
        where V2 : IEquatable<V2>
    {
        public ResizeableSubArray<Weighted<V1>> elements1;// = new ResizeableSubArray<Weighted<T1>>(0);
        public ResizeableSubArray<Weighted<V2>> elements2;// = new ResizeableSubArray<Weighted<T2>>(0);

        public bool Equals(JoinState<V1, V2> that)
        {
            if (this.elements1.Count != that.elements1.Count || this.elements2.Count != that.elements2.Count)
                return false;

            for (int i = 0; i < elements1.Count; i++)
                if (!elements1.Array[i].Equals(that.elements1.Array[i]))
                    return false;

            for (int i = 0; i < elements2.Count; i++)
                if (!elements2.Array[i].Equals(that.elements2.Array[i]))
                    return false;

            return true;
        }
    }

    public class Join<T1, T2, K, V1, V2, R> : BinaryCollectionShard<Weighted<T1>, Weighted<T2>, K, Weighted<V1>, Weighted<V2>, JoinState<V1,V2>, Weighted<R>>
        where T1 : IEquatable<T1>
        where T2 : IEquatable<T2>
        where V1 : IEquatable<V1>
        where V2 : IEquatable<V2>
        where K : IEquatable<K>
        where R : IEquatable<R>
    {
        Func<K, V1, V2, R> resultSelector;

        Dictionary<V1, Int64> input1Accumulator = new Dictionary<V1, Int64>();
        Dictionary<V2, Int64> input2Accumulator = new Dictionary<V2, Int64>();

        ResizeableSubArray<Weighted<V1>> updates1 = new ResizeableSubArray<Weighted<V1>>();
        ResizeableSubArray<Weighted<V2>> updates2 = new ResizeableSubArray<Weighted<V2>>();

        //protected override  S UpdateState(K key, S state, int updateRootIndex1, int updateRootIndex2) { return state; }
#if false
        protected override JoinState<V1, V2> UpdateState(K key, JoinState<V1, V2> state, int updateRootIndex1, int updateRootIndex2)
        {
            if (state.elements1.Array == null) state.elements1 = new ResizeableSubArray<Weighted<V1>>(0);
            if (state.elements2.Array == null) state.elements2 = new ResizeableSubArray<Weighted<V2>>(0);

            var elements1 = state.elements1.Array;   // makes things look a bit cleaner
            var elements2 = state.elements2.Array;   // makes things look a bit cleaner

            updates1.Clear();
            for (int index = updateRootIndex1; index >= 0; index = updateChain1[index].previous)
                updates1.Add(updateChain1[index].update);

            updates2.Clear();
            for (int index = updateRootIndex2; index >= 0; index = updateChain2[index].previous)
                updates2.Add(updateChain2[index].update);

            //var updates1 = update.inputs1.Array;            // makes things look a bit cleaner
            //var updates2 = update.inputs2.Array;            // makes things look a bit cleaner

            // these sums assume that db weights do not go negative
            Int64 oldNormalization = 0;
            for (int i = 0; i < state.elements1.Count; i++) oldNormalization += elements1[i].weight;
            for (int i = 0; i < state.elements2.Count; i++) oldNormalization += elements2[i].weight;

            // compute new normalization from the old
            var newNormalization = oldNormalization;
            for (int i = 0; i < updates1.Count; i++) newNormalization += updates1.Array[i].weight;
            for (int i = 0; i < updates2.Count; i++) newNormalization += updates2.Array[i].weight;

            if (oldNormalization != newNormalization)
            {
                for (int i = 0; i < state.elements1.Count; i++)
                {
                    for (int j = 0; j < state.elements2.Count; j++)
                    {
                        var product = (((double)elements1[i].weight)) * (((double)elements2[j].weight));

                        Int64 result = 0;
                        if (oldNormalization != 0)
                            result -= (Int64)(product / oldNormalization);

                        if (newNormalization != 0)
                            result += (Int64)(product / newNormalization);

                        if (result != 0)
                            Send(new Weighted<R>(resultSelector(key, elements1[i].record, elements2[j].record), result));

                        if (result < -(1L << 58))
                            Console.WriteLine("!!!!");
                    }
                }
            }

            if (newNormalization != 0)
            {
                // var newReciprocal = 1.0 / newNormalization;
                // match updates1 against elements2
                for (int i = 0; i < updates1.Count; i++)
                    if (updates1.Array[i].weight != 0)
                        for (int j = 0; j < state.elements2.Count; j++)
                            if (elements2[j].weight != 0)
                            {
                                var product = (((double)updates1.Array[i].weight)) * (((double)elements2[j].weight));
                                var result = (Int64)(product / newNormalization);

                                if (result != 0)
                                    Send(new Weighted<R>(resultSelector(key, updates1.Array[i].record, elements2[j].record), result));

                                if (result < -(1L << 58))
                                    Console.WriteLine("!!!!");

                            }

                // match elements1 against updates2
                for (int i = 0; i < updates2.Count; i++)
                    if (updates2.Array[i].weight != 0)
                        for (int j = 0; j < state.elements1.Count; j++)
                            if (elements1[j].weight != 0)
                            {
                                var product = (((double)updates2.Array[i].weight)) * (((double)elements1[j].weight));
                                var result = (Int64)(product / newNormalization);

                                if (result != 0)
                                    Send(new Weighted<R>(resultSelector(key, elements1[j].record, updates2.Array[i].record), result));

                                if (result < -(1L << 58))
                                    Console.WriteLine("!!!!");

                            }

                // match updates1 against updates2
                for (int i = 0; i < updates1.Count; i++)
                    if (updates1.Array[i].weight != 0)
                        for (int j = 0; j < updates2.Count; j++)
                            if (updates2.Array[j].weight != 0)
                            {
                                var product = (((double)updates1.Array[i].weight)) * (((double)updates2.Array[j].weight));
                                var result = (Int64)(product / newNormalization);

                                if (result != 0)
                                    Send(new Weighted<R>(resultSelector(key, updates1.Array[i].record, updates2.Array[j].record), result));

                                if (result < -(1L << 58))
                                    Console.WriteLine("!!!!");

                            }
            }

            #region Folding updates1 into elements1

            for (int i = 0; i < updates1.Count; i++)
            {
                if (!input1Accumulator.ContainsKey(updates1.Array[i].record))
                    input1Accumulator.Add(updates1.Array[i].record, updates1.Array[i].weight);
                else
                    input1Accumulator[updates1.Array[i].record] += updates1.Array[i].weight;
            }

            for (int i = 0; i < state.elements1.Count; i++)
            {
                if (input1Accumulator.ContainsKey(elements1[i].record))
                {
                    elements1[i].weight += input1Accumulator[elements1[i].record];
                    input1Accumulator.Remove(elements1[i].record);
                }

                if (elements1[i].weight == 0)
                {
                    state.elements1.Count--;
                    state.elements1.Array[i] = state.elements1.Array[state.elements1.Count];
                    i--;
                }
            }

            if (state.elements1.Count == 0)
                state.elements1.Clear();


            foreach (var entry in input1Accumulator)
                state.elements1.Add(new Weighted<V1>(entry.Key, entry.Value));

            input1Accumulator.Clear();

            #endregion

            #region Folding updates2 into elements2

            for (int i = 0; i < updates2.Count; i++)
            {
                if (!input2Accumulator.ContainsKey(updates2.Array[i].record))
                    input2Accumulator.Add(updates2.Array[i].record, updates2.Array[i].weight);
                else
                    input2Accumulator[updates2.Array[i].record] += updates2.Array[i].weight;
            }

            for (int i = 0; i < state.elements2.Count; i++)
            {
                if (input2Accumulator.ContainsKey(elements2[i].record))
                {
                    elements2[i].weight += input2Accumulator[elements2[i].record];
                    input2Accumulator.Remove(elements2[i].record);
                }

                if (elements2[i].weight == 0)
                {
                    state.elements2.Count--;
                    state.elements2.Array[i] = state.elements2.Array[state.elements2.Count];
                    i--;
                }
            }

            if (state.elements2.Count == 0)
                state.elements2.Clear();

            foreach (var entry in input2Accumulator)
                state.elements2.Add(new Weighted<V2>(entry.Key, entry.Value));

            input2Accumulator.Clear();

            #endregion

            return state;
        }
#else
        protected override JoinState<V1, V2> UpdateState(K key, JoinState<V1, V2> state, int updateRootIndex1, int updateRootIndex2)
        {
            if (state.elements1.Array == null) state.elements1 = new ResizeableSubArray<Weighted<V1>>(0);
            if (state.elements2.Array == null) state.elements2 = new ResizeableSubArray<Weighted<V2>>(0);

            var elements1 = state.elements1.Array;   // makes things look a bit cleaner
            var elements2 = state.elements2.Array;   // makes things look a bit cleaner

            var results = new Dictionary<R, Int64>();
            
            // these sums assume that db weights do not go negative
            Int64 oldNormalization = 0;
            for (int i = 0; i < state.elements1.Count; i++) oldNormalization += elements1[i].weight;
            for (int i = 0; i < state.elements2.Count; i++) oldNormalization += elements2[i].weight;

            for (int i = 0; i < state.elements1.Count; i++)
            {
                for (int j = 0; j < state.elements2.Count; j++)
                {
                    var product = (((double)elements1[i].weight)) * (((double)elements2[j].weight));

                    Int64 result = (Int64)(product / oldNormalization);

                    var element = resultSelector(key, elements1[i].record, elements2[j].record);
                    if (!results.ContainsKey(element))
                        results.Add(element, 0);

                    results[element] -= result;
                }
            }

            #region Folding updates1 into elements1
            
            //updates1.Clear();
            for (int index = updateRootIndex1; index >= 0; index = updateChain1[index].previous)
            {
                //updates1.Add(updateChain1[index].update);
                if (!input1Accumulator.ContainsKey(updateChain1[index].update.record))
                    input1Accumulator.Add(updateChain1[index].update.record, updateChain1[index].update.weight);
                else
                    input1Accumulator[updateChain1[index].update.record] += updateChain1[index].update.weight;
            }

            for (int i = 0; i < state.elements1.Count; i++)
            {
                if (input1Accumulator.ContainsKey(elements1[i].record))
                {
                    elements1[i].weight += input1Accumulator[elements1[i].record];
                    input1Accumulator.Remove(elements1[i].record);
                }

                if (elements1[i].weight == 0)
                {
                    state.elements1.Count--;
                    state.elements1.Array[i] = state.elements1.Array[state.elements1.Count];
                    i--;
                }
            }

            if (state.elements1.Count == 0)
                state.elements1.Clear();


            foreach (var entry in input1Accumulator)
                state.elements1.Add(new Weighted<V1>(entry.Key, entry.Value));

            input1Accumulator.Clear();



            #endregion

            #region Folding updates2 into elements2

            //updates2.Clear();
            for (int index = updateRootIndex2; index >= 0; index = updateChain2[index].previous)
            //    updates2.Add(updateChain2[index].update);
            //for (int i = 0; i < updates2.Count; i++)
            {
                if (!input2Accumulator.ContainsKey(updateChain2[index].update.record))
                    input2Accumulator.Add(updateChain2[index].update.record, updateChain2[index].update.weight);
                else
                    input2Accumulator[updateChain2[index].update.record] += updateChain2[index].update.weight;
            }

            for (int i = 0; i < state.elements2.Count; i++)
            {
                if (input2Accumulator.ContainsKey(elements2[i].record))
                {
                    elements2[i].weight += input2Accumulator[elements2[i].record];
                    input2Accumulator.Remove(elements2[i].record);
                }

                if (elements2[i].weight == 0)
                {
                    state.elements2.Count--;
                    state.elements2.Array[i] = state.elements2.Array[state.elements2.Count];
                    i--;
                }
            }

            if (state.elements2.Count == 0)
                state.elements2.Clear();

            foreach (var entry in input2Accumulator)
                state.elements2.Add(new Weighted<V2>(entry.Key, entry.Value));

            input2Accumulator.Clear();

            #endregion

            // these sums assume that db weights do not go negative
            Int64 newNormalization = 0;
            for (int i = 0; i < state.elements1.Count; i++) newNormalization += state.elements1.Array[i].weight;
            for (int i = 0; i < state.elements2.Count; i++) newNormalization += state.elements2.Array[i].weight;

            for (int i = 0; i < state.elements1.Count; i++)
            {
                for (int j = 0; j < state.elements2.Count; j++)
                {
                    var product = (((double)state.elements1.Array[i].weight)) * (((double)state.elements2.Array[j].weight));

                    Int64 result = (Int64)(product / newNormalization);

                    var element = resultSelector(key, state.elements1.Array[i].record, state.elements2.Array[j].record);
                    if (!results.ContainsKey(element))
                        results.Add(element, 0);

                    results[element] += result;
                }
            }

            foreach (var pair in results)
                if (pair.Value != 0)
                    Send(new Weighted<R>(pair.Key, pair.Value));
         
            return state;
        }
#endif
        public override IEnumerable<Weighted<R>> Contents()
        {
            foreach (var pair in states)
            {
                var entry = pair.Value;
                var key = pair.Key;
                Int64 normalization = 0; // old sum of absolute values of elements in both left and right sides

                // these sums assume that db weights do not go negative
                for (int i = 0; i < entry.elements1.Count; i++)
                    normalization += entry.elements1.Array[i].weight;

                for (int i = 0; i < entry.elements2.Count; i++)
                    normalization += entry.elements2.Array[i].weight;

                if (normalization != 0)
                {
                    for (int i = 0; i < entry.elements1.Count; i++)
                    {
                        for (int j = 0; j < entry.elements2.Count; j++)
                        {
                            var product = (((double)entry.elements1.Array[i].weight)) * (((double)entry.elements2.Array[j].weight));
                            var resultW = (Int64)(product / normalization);

                            var result = resultSelector(key, entry.elements1.Array[i].record, entry.elements2.Array[j].record);

                            if (resultW != 0)
                                yield return new Weighted<R>(result, resultW);
                        }
                    }
                }
            }
        }

        public Join(CollectionShard<Weighted<T1>> source1, Channel<Weighted<T1>> channel1, 
                    CollectionShard<Weighted<T2>> source2, Channel<Weighted<T2>> channel2, 
                    Func<T1, K> keyFunc1, Func<T2, K> keyFunc2,
                    Func<T1, V1> valFunc1, Func<T2, V2> valFunc2, 
                    Func<K, V1, V2, R> resultSel)
            : base("Join", source1, channel1, x => keyFunc1(x.record), x => new Weighted<V1>(valFunc1(x.record), x.weight), 
                           source2, channel2, x => keyFunc2(x.record), x => new Weighted<V2>(valFunc2(x.record), x.weight))
        {
            resultSelector = resultSel;
        }
    }
}
