using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ
{
    public struct ResizeableSubArray<S>
    {
        public S[] Array;
        public int Count;

        public void EnsureCapacity(int size)
        {
            if (Array.Length < size)
            {
                var newArraySize = Math.Max(2 * Array.Length, 4);
                while (newArraySize < size)
                    newArraySize = 2 * newArraySize;

                var newArray = ThreadLocalBufferPools<S>.pool.Value.CheckOut(newArraySize);

                for (int i = 0; i < Count; i++)
                    newArray[i] = Array[i];

                ThreadLocalBufferPools<S>.pool.Value.CheckIn(Array);

                Array = newArray;
            }
        }

        public void EnsureAvailable(int size)
        {
            EnsureCapacity(Count + size);
        }

        public void Clear()
        {
            ThreadLocalBufferPools<S>.pool.Value.CheckIn(Array);
            Array = ThreadLocalBufferPools<S>.pool.Value.Empty;
            Count = 0;
        }

        public void Add(S element)
        {
            EnsureAvailable(1);
            Array[Count++] = element;
        }

        // don't make one of these if you don't have data
        public ResizeableSubArray(int size)
        {
            Count = 0;
            Array = size > 0 ? ThreadLocalBufferPools<S>.pool.Value.CheckOut(size) : ThreadLocalBufferPools<S>.pool.Value.Empty;
        }
    }



    public struct SubArray<T> : IEquatable<SubArray<T>>
    {
        public T[] Array;
        public int Count;

        public SubArray(T[] a) { Array = a; Count = Array == null ? 0 : Array.Length; }
        public SubArray(T[] a, int c) { Array = a; Count = c; }

        public bool Equals(SubArray<T> that)
        {
            return this.Array == that.Array && this.Count == that.Count;
        }

        public int Length
        {
            get { return Array == null ? 0 : Array.Length; }
        }

        public T this[int i]
        {
            get { return Array[i]; }
            set { Array[i] = value; }
        }

        public int Available
        {
            get { return Length - Count; }
            set { Count = Length - value; }
        }
    }
}
