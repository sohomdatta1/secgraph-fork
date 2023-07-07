using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ.Operators
{
    public class InputCollection<T> : PINQCollection<T>, IObserver<IEnumerable<Weighted<T>>> where T : IEquatable<T>
    {
        public Task[] inputShards()
        {
            return shards;
        }

        public void OnNext(IEnumerable<Weighted<T>> next)
        {
            var index = 0;
            foreach (var element in next)
                if (element.weight != 0)
                    (shards[index++ % shards.Length] as Input<T>).Add(element);

            Start();
        }

        public void OnError(Exception e) { }
        public void OnCompleted() { }

        public InputCollection(int parallelism) : base(parallelism) { }
    }


    class Input<T> : CollectionShard<Weighted<T>> where T : IEquatable<T>
    {
        List<Weighted<T>> elements;
        List<Weighted<T>> pending;

        public void Add(IEnumerable<Weighted<T>> more)
        {
            pending.AddRange(more);
        }

        public void Add(Weighted<T> more)
        {
            pending.Add(more);
        }
        public override IEnumerable<Weighted<T>> Contents()
        {
            return elements;
        }

        public override Task[] Work()
        {
            for (int i = 0; i < pending.Count; i++)
                Send(pending[i]);

            DoneSending();

            elements.AddRange(pending);

            pending.Clear();

            return dependents.ToArray();
        }

        public Input(IEnumerable<Weighted<T>> input)
            : base("input") 
        { 
            pending = input.ToList(); elements = new List<Weighted<T>>();
        }
    }
}
