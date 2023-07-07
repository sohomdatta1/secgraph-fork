using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ.Operators
{
    public class Observation
    {
        public static Object TotalErrorLock = new object();
        public static double TotalError;

        public static System.Threading.ThreadLocal<Random> random = new System.Threading.ThreadLocal<Random>(() => new Random(System.Threading.Thread.CurrentThread.ManagedThreadId));
        
        public Int64 truth;
        public Int64 value;
        public Int64 noise;

        public double epsilon;

        public Observation(double eps) 
        { 
            epsilon = eps;

            noise = (Int64)(Int32.MaxValue * Math.Log(random.Value.NextDouble()) * ((random.Value.Next() % 2) == 0 ? 1.0 / epsilon : -1.0 / epsilon));
        }
    }

    public class CountCollection<K, T> : PINQCollection<T>, Count<K> where T : IEquatable<T>
    {
        public double this[K index]
        {
            get 
            {
                var shard = shards[(Int32.MaxValue & index.GetHashCode()) % shards.Length] as Count<K, T>;
                return shard[index]; 
            }
        }

        public CountCollection(int parallelism) : base(parallelism) { }
    }

    public interface Count<K>
    {
        double this[K index] { get; }
    }

    public class Count<K, T> : UnaryCollectionShard<Weighted<T>, Weighted<T>> where T : IEquatable<T>
    {
        Func<T, K> key;
        Dictionary<K, Observation> observations = new Dictionary<K,Observation>();

        double epsilon;

        public double this[K index]
        {
            get
            {
                // we may need to introduce observations we didn't actually make.
                // shouldn't be a problem, as long as inference values before and
                // after the measurement are not compared. 
                if (!observations.ContainsKey(index))
                {
                    observations.Add(index, new Observation(epsilon));
                    TotalErrorChange += (Int64) (observations[index].epsilon * Math.Abs(observations[index].noise));
                    
                    lock (Observation.TotalErrorLock)
                    {
                        Observation.TotalError += ((double)TotalErrorChange) / Int32.MaxValue;
                        TotalErrorChange = 0;
                    }
                }

                // we only ever report the value we observed, truth + noise.
                return ((double)(observations[index].truth + observations[index].noise)) / Int32.MaxValue;
            }
        }

        Int64 TotalErrorChange = 0;
        public override void OnInput1(Weighted<T> record)
        {
            var k = key(record.record);

            if (!observations.ContainsKey(k))
            {
                observations.Add(k, new Observation(epsilon));

                // we are about to subtract it out, so we want to pre-load the TotalErrorChange with the initial value.
               // TotalErrorChange += (Int64)(observations[k].epsilon * Math.Abs(observations[k].noise));
            }

            TotalErrorChange -= (Int64) (observations[k].epsilon * Math.Abs(observations[k].truth + observations[k].noise - observations[k].value));

            observations[k].value += record.weight;

            TotalErrorChange += (Int64) (observations[k].epsilon * Math.Abs(observations[k].truth + observations[k].noise - observations[k].value));
        }

        // push total change into global
        bool initialized = false;
        protected override void Compute()
        {
            // first time around, lock off all the values.
            if (!initialized)
            {
                foreach (var observation in observations.Values)
                {
                    TotalErrorChange -= (Int64) (observation.epsilon * Math.Abs(observation.truth + observation.noise - observation.value));

                    observation.truth = observation.value;

                    TotalErrorChange += (Int64) (observation.epsilon * Math.Abs(observation.truth + observation.noise - observation.value));
                }

                initialized = true;
            }
            
            lock (Observation.TotalErrorLock)
            {
                //Console.WriteLine("Updating TOTAL ERROR from {0} to {1}", Observation.TotalError, Observation.TotalError + (((double)TotalErrorChange) / Int32.MaxValue));
                //Console.WriteLine("Updating TOTAL ERROR from {0} to {1} by {2}", Observation.TotalError, Observation.TotalError + (((double)TotalErrorChange) / Int32.MaxValue), TotalErrorChange);

                Observation.TotalError += ((double)TotalErrorChange) / Int32.MaxValue;
                TotalErrorChange = 0;
            }
        }

        public Count(CollectionShard<Weighted<T>> source1, Channel<Weighted<T>> channel1, Func<T, K> k, double eps)
            : base("Count", source1, channel1)
        {
            key = k;
            epsilon = eps;
        }
    }
}
