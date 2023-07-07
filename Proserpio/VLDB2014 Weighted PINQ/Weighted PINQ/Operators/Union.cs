using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ.Operators
{
    public struct UnionState : IEquatable<UnionState>
    {
        public Int64 val1;
        public Int64 val2;


        public bool Equals(UnionState that)
        {
            return (this.val1 == that.val1 && this.val2 == that.val2);
        }
    }

    class Union<T> : BinaryCollectionShard<Weighted<T>, Weighted<T>, T, UnionState, Weighted<T>> where T : IEquatable<T>
    {
        protected override UnionState UpdateState(T key, UnionState state, int updateRootIndex1, int updateRootIndex2)
        {
            var oldmax = Math.Max(state.val1, state.val2);

            // fold in new weight to the first value
            for (int i = updateRootIndex1; i >= 0; i = updateChain1[i].previous)
                state.val1 += updateChain1[i].update.weight;

            // fold in new weight to the first value
            for (int i = updateRootIndex2; i >= 0; i = updateChain2[i].previous)
                state.val2 += updateChain2[i].update.weight;

            var newmax = Math.Max(state.val1, state.val2);

            if (oldmax != newmax)
                Send(new Weighted<T>(key, newmax - oldmax));

            return state;
        }
#if false
        protected override void UpdateState(Update update)
        {
            UnionState state;

            var present = states.TryGetValue(update.key, out state);

            if (!present) state = new UnionState();
            
            var oldmax = Math.Max(state.val1, state.val2);

            // fold in new weight to the first value
            for (int i = 0; i < update.inputs1.Count; i++)
                state.val1 += update.inputs1.Array[i].weight;

            // fold in new weight to the second value
            for (int i = 0; i < update.inputs2.Count; i++)
                state.val2 += update.inputs2.Array[i].weight;
    
            var newmax = Math.Max(state.val1, state.val2);

            if (oldmax != newmax)
                Send(new Weighted<T>(update.key, newmax - oldmax));


            if (!present)
                states.Add(update.key, state);
            else
                states[update.key] = state;
    
        }
#endif
        public override IEnumerable<Weighted<T>> Contents()
        {
            return states.Select(pair => new Weighted<T>(pair.Key, Math.Max(pair.Value.val1, pair.Value.val2)));
        }


        public Union(CollectionShard<Weighted<T>> source1, 
                     Channel<Weighted<T>> channel1, 
                     CollectionShard<Weighted<T>> source2, 
                     Channel<Weighted<T>> channel2)
            : base("Union", source1, channel1, x => x.record, source2, channel2, x => x.record)
        { 
        
        }
    }
}
