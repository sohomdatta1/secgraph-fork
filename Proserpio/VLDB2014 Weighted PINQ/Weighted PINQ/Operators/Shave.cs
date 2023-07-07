using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ.Operators
{
    public struct ShaveState : IEquatable<ShaveState>
    {
        public int index;      // indicates the number of complete intervals
        public Int64 value;   // indicates weight towards the next interval

        public bool Equals(ShaveState that)
        {
            return this.index == that.index && this.value == that.value;
        }
    }

    public class Shave<T, R> : UnaryCollectionShard<Weighted<T>, T, ShaveState, Weighted<R>>
        where T : IEquatable<T>
        where R : IEquatable<R>
    {
        Func<int, T, R> reducer;
        //how thick is the ith slice of the key (records)
        Func<int, T, Int64> GetIncrement;

        //please tell everything you already know...
        public override IEnumerable<Weighted<R>> Contents()
        {
            foreach (var state in states)
            {
                for (int i = 0; i < state.Value.index; i++)
                    yield return new Weighted<R>(reducer(i, state.Key), GetIncrement(i, state.Key));

                if (state.Value.value > 0.0)
                    yield return new Weighted<R>(reducer(state.Value.index, state.Key), state.Value.value);
            }
        }

#if false
        protected override ShaveState UpdateState(T key, ShaveState state, StateUpdate update)
        {
            var weight = 0.0;
            for (int i = 0; i < update.inputs1.Count; i++)
                weight += update.inputs1[i].weight;

#else
        protected override ShaveState UpdateState(T key, ShaveState state, int update)
        {
            Int64 weight = 0;

            //Console.WriteLine("update: {0}", update);

            var index = update;
            while (index >= 0)
            {
                //all diffs we have received
                weight += updateChain[index].update.weight;
                index = updateChain[index].previous;
            }

            //Console.WriteLine("weight: {0}", weight);
#endif

            //var weight = update.update.weight;
            //for (int i = 0; update.additionalUpdates != null && i < update.additionalUpdates.Count; i++)
            //    weight += update.additionalUpdates[i].weight;

            // Console.WriteLine("Weight: " + weight);

            while (weight > 0)
            {
                var delta = Math.Min(weight, GetIncrement(state.index, key) - state.value);
                //state.index = index and reminder
                Send(new Weighted<R>(reducer(state.index, key), delta));

                weight -= delta;
                state.value += delta;

                if (weight > 0.0)
                {
                    state.index += 1;
                    state.value = 0;
                }
            }

            while (weight < 0)
            {
                var delta = Math.Max(weight, 0 - state.value);

                Send(new Weighted<R>(reducer(state.index, key), delta));

                weight -= delta;
                state.value += delta;

                if (weight < 0)
                {
                    state.index -= 1;
                    state.value = GetIncrement(state.index, key);
                }
            }

            return state;
        }

        public Shave(CollectionShard<Weighted<T>> source1, Channel<Weighted<T>> channel1, Func<int,T,double> increment, Func<int, T, R> r)
            : base("Shave", source1, channel1, x => x.record)
        {
            reducer = r;
            GetIncrement = (x,y) => ((Int64)(increment(x,y) * Int32.MaxValue));
        }
    }
}
