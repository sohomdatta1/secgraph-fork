using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ.Operators
{
    public struct IntersectState : IEquatable<IntersectState>
    {
        public Int64 val1;
        public Int64 val2;

        public bool Equals(IntersectState that)
        {
            return (this.val1 == that.val1 && this.val2 == that.val2);
        }

        public override string ToString()
        {
            return string.Format("({0}, {1})", val1, val2);
        }
    }

  class Intersect<T> : BinaryCollectionShard<Weighted<T>, Weighted<T>, T, IntersectState, Weighted<T>> where T : IEquatable<T>
  {
      protected override IntersectState UpdateState(T key, IntersectState state, int updateRootIndex1, int updateRootIndex2)
      {
          var oldmin = Math.Min(state.val1, state.val2);

          // fold in new weight to the first value
          for (int i = updateRootIndex1; i >= 0; i = updateChain1[i].previous)
              state.val1 += updateChain1[i].update.weight;

          // fold in new weight to the first value
          for (int i = updateRootIndex2; i >= 0; i = updateChain2[i].previous)
              state.val2 += updateChain2[i].update.weight;

          var newmin = Math.Min(state.val1, state.val2);

          if (oldmin != newmin)
              Send(new Weighted<T>(key, newmin - oldmin));

          return state;
      }
#if false
      public override void OnInput1(Weighted<T> input1)
      {
          IntersectState state;

          var key = input1.record;

          var present = states.TryGetValue(input1.record, out state);

          if (!present)
              state = new IntersectState();

          var oldmin = Math.Min(state.val1, state.val2);

          state.val1 += input1.weight;

          var newmin = Math.Min(state.val1, state.val2);

          if (oldmin != newmin)
              Send(new Weighted<T>(input1.record, newmin - oldmin));

          if (state.val1 == 0.0 && state.val2 == 0.0)
          {
              if (present)
                  states.Remove(key);
          }
          else
          {
              if (!present)
                  states.Add(key, state);
              else
                  states[key] = state;
          }
      }

      public override void OnInput2(Weighted<T> input2)
      {
          IntersectState state;

          var key = input2.record;

          var present = states.TryGetValue(input2.record, out state);

          if (!present)
              state = new IntersectState();

          var oldmin = Math.Min(state.val1, state.val2);

          state.val2 += input2.weight;

          var newmin = Math.Min(state.val1, state.val2);

          if (oldmin != newmin)
              Send(new Weighted<T>(key, newmin - oldmin));

          if (state.val1 == 0.0 && state.val2 == 0.0)
          {
              if (present)
                  states.Remove(key);
          }
          else
          {
              if (!present)
                  states.Add(key, state);
              else
                  states[key] = state;
          }
      }


      protected override void UpdateState(Update update)
      {
          IntersectState state;

          var present = states.TryGetValue(update.key, out state);

          if (!present) state = new IntersectState();
            

          var oldmin = Math.Min(state.val1, state.val2);

          // fold in new weight to the first value
          for (int i = 0; i < update.inputs1.Count; i++)
              state.val1 += update.inputs1.Array[i].weight;
          
          // fold in new weight to the second value
          for (int i = 0; i < update.inputs2.Count; i++)
              state.val2 += update.inputs2.Array[i].weight;
        
          var newmin = Math.Min(state.val1, state.val2);

          if (oldmin != newmin)
              Send(new Weighted<T>(update.key, newmin - oldmin));


          if (!present)
              states.Add(update.key, state);
          else
              states[update.key] = state;
      }
#endif
      public override IEnumerable<Weighted<T>> Contents()
      {
          return states.Select(pair => new Weighted<T>(pair.Key, Math.Min(pair.Value.val1, pair.Value.val2)));
      }

      public Intersect(CollectionShard<Weighted<T>> source1,
                       Channel<Weighted<T>> channel1,
                       CollectionShard<Weighted<T>> source2,
                       Channel<Weighted<T>> channel2)
          : base("Intersect", source1, channel1, x => x.record, source2, channel2, x => x.record)
      {
      }
  }
}
