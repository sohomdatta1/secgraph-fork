using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using System.Threading;

namespace Microsoft.Research.WeightedPINQ
{
    // thread-local buffer pools
    static public class ThreadLocalBufferPools<T>
    {
        public static ThreadLocal<BufferPool<T>> pool = new ThreadLocal<BufferPool<T>>(() => new BufferPool<T>(16));
    }
   
    public class BufferPool<T>
    {
        public const int MaximumStackLength = 2 * 1024;

        Stack<T[]>[] stacks;    // indexed by log of the buffer size.

        public static int Log2(int x)
        {
            var result = 0;

            if (x >> (result + 16) != 0) result += 16;
            if (x >> (result + 8) != 0) result += 8;
            if (x >> (result + 4) != 0) result += 4;
            if (x >> (result + 2) != 0) result += 2;
            if (x >> (result + 1) != 0) result += 1;

            return result;
        }

        public int Count
        {
            get
            {
                var result = 0;
                for (int i = 0; i < stacks.Length; i++)
                    result += stacks[i].Count;

                return result;
            }
        }

        public T[] CheckOut(int size)
        {
            if (1 << Log2(size) != size)
                Console.WriteLine("{0} {1}", size, Log2(size));

            size = Log2(size);

            if (size < stacks.Length && stacks[size].Count > 0)
                return stacks[size].Pop();
            else
                return new T[1 << size];
        }

        public T[] Empty = new T[0];

        public void CheckIn(T[] array)
        {
            if (array != null && array.Length > 0)
            {
                var size = Log2(array.Length);

                if (size < stacks.Length)
                {
                    if (stacks[size].Count < MaximumStackLength)
                        stacks[size].Push(array);
                }
            }
        }

        public BufferPool(int maxsize)
        {
            stacks = new Stack<T[]>[maxsize + 1];
            for (int i = 0; i < stacks.Length; i++)
                stacks[i] = new Stack<T[]>(1);
        }
    }
}
