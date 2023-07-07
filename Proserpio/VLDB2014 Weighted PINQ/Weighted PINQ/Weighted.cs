using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ
{
    public struct Weighted<T> where T : IEquatable<T>
    {
        public T record;
        public Int64 weight;

        public override string ToString()
        {
            return String.Format("[{0}, {1}]", record, ((double)weight)/Int32.MaxValue);
        }

        public Weighted<T> Negate() { return new Weighted<T>(record, -weight); }

        public Weighted(T r, Int64 w) { record = r; weight = w; }
        public Weighted(T r, double w) { record = r; weight = (Int64) (w * Int32.MaxValue); }
    }
}
