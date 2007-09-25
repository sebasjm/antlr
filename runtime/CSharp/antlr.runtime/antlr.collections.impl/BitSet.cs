namespace antlr.collections.impl
{
    using System;
    using System.Collections;

    public class BitSet : ICloneable
    {
        protected internal const int BITS = 0x40;
        protected internal long[] dataBits;
        protected internal const int LOG_BITS = 6;
        protected internal static readonly int MOD_MASK = 0x3f;
        protected internal const int NIBBLE = 4;

        public BitSet() : this(0x40)
        {
        }

        public BitSet(long[] bits_)
        {
            this.dataBits = bits_;
        }

        public BitSet(int nbits)
        {
            this.dataBits = new long[((nbits - 1) >> 6) + 1];
        }

        public virtual void add(int el)
        {
            int index = wordNumber(el);
            if (index >= this.dataBits.Length)
            {
                this.growToInclude(el);
            }
            this.dataBits[index] |= bitMask(el);
        }

        public virtual BitSet and(BitSet a)
        {
            BitSet set = (BitSet) this.Clone();
            set.andInPlace(a);
            return set;
        }

        public virtual void andInPlace(BitSet a)
        {
            int num2;
            int num = Math.Min(this.dataBits.Length, a.dataBits.Length);
            for (num2 = num - 1; num2 >= 0; num2--)
            {
                this.dataBits[num2] &= a.dataBits[num2];
            }
            for (num2 = num; num2 < this.dataBits.Length; num2++)
            {
                this.dataBits[num2] = 0L;
            }
        }

        private static long bitMask(int bitNumber)
        {
            int num = bitNumber & MOD_MASK;
            return (((long) 1L) << num);
        }

        public virtual void clear()
        {
            for (int i = this.dataBits.Length - 1; i >= 0; i--)
            {
                this.dataBits[i] = 0L;
            }
        }

        public virtual void clear(int el)
        {
            int index = wordNumber(el);
            if (index >= this.dataBits.Length)
            {
                this.growToInclude(el);
            }
            this.dataBits[index] &= ~bitMask(el);
        }

        public virtual object Clone()
        {
            BitSet set;
            try
            {
                set = new BitSet();
                set.dataBits = new long[this.dataBits.Length];
                Array.Copy(this.dataBits, 0, set.dataBits, 0, this.dataBits.Length);
            }
            catch
            {
                throw new ApplicationException();
            }
            return set;
        }

        public virtual int degree()
        {
            int num = 0;
            for (int i = this.dataBits.Length - 1; i >= 0; i--)
            {
                long num3 = this.dataBits[i];
                if (num3 != 0L)
                {
                    for (int j = 0x3f; j >= 0; j--)
                    {
                        if ((num3 & (((long) 1L) << j)) != 0L)
                        {
                            num++;
                        }
                    }
                }
            }
            return num;
        }

        public override bool Equals(object obj)
        {
            if ((obj == null) || !(obj is BitSet))
            {
                return false;
            }
            BitSet set = (BitSet) obj;
            int num = Math.Min(this.dataBits.Length, set.dataBits.Length);
            int index = num;
            while (index-- > 0)
            {
                if (this.dataBits[index] != set.dataBits[index])
                {
                    return false;
                }
            }
            if (this.dataBits.Length > num)
            {
                index = this.dataBits.Length;
                while (index-- > num)
                {
                    if (this.dataBits[index] != 0L)
                    {
                        return false;
                    }
                }
            }
            else if (set.dataBits.Length > num)
            {
                index = set.dataBits.Length;
                while (index-- > num)
                {
                    if (set.dataBits[index] != 0L)
                    {
                        return false;
                    }
                }
            }
            return true;
        }

        public override int GetHashCode()
        {
            return this.dataBits.GetHashCode();
        }

        public virtual void growToInclude(int bit)
        {
            long[] destinationArray = new long[Math.Max(this.dataBits.Length << 1, this.numWordsToHold(bit))];
            Array.Copy(this.dataBits, 0, destinationArray, 0, this.dataBits.Length);
            this.dataBits = destinationArray;
        }

        public virtual int lengthInLongWords()
        {
            return this.dataBits.Length;
        }

        public virtual bool member(int el)
        {
            int index = wordNumber(el);
            if (index >= this.dataBits.Length)
            {
                return false;
            }
            return ((this.dataBits[index] & bitMask(el)) != 0L);
        }

        public virtual bool nil()
        {
            for (int i = this.dataBits.Length - 1; i >= 0; i--)
            {
                if (this.dataBits[i] != 0L)
                {
                    return false;
                }
            }
            return true;
        }

        public virtual BitSet not()
        {
            BitSet set = (BitSet) this.Clone();
            set.notInPlace();
            return set;
        }

        public virtual void notInPlace()
        {
            for (int i = this.dataBits.Length - 1; i >= 0; i--)
            {
                this.dataBits[i] = ~this.dataBits[i];
            }
        }

        public virtual void notInPlace(int maxBit)
        {
            this.notInPlace(0, maxBit);
        }

        public virtual void notInPlace(int minBit, int maxBit)
        {
            this.growToInclude(maxBit);
            for (int i = minBit; i <= maxBit; i++)
            {
                int index = wordNumber(i);
                this.dataBits[index] ^= bitMask(i);
            }
        }

        private int numWordsToHold(int el)
        {
            return ((el >> 6) + 1);
        }

        public static BitSet of(int el)
        {
            BitSet set = new BitSet(el + 1);
            set.add(el);
            return set;
        }

        public virtual BitSet or(BitSet a)
        {
            BitSet set = (BitSet) this.Clone();
            set.orInPlace(a);
            return set;
        }

        public virtual void orInPlace(BitSet a)
        {
            if (a.dataBits.Length > this.dataBits.Length)
            {
                this.setSize(a.dataBits.Length);
            }
            for (int i = Math.Min(this.dataBits.Length, a.dataBits.Length) - 1; i >= 0; i--)
            {
                this.dataBits[i] |= a.dataBits[i];
            }
        }

        public virtual void remove(int el)
        {
            int index = wordNumber(el);
            if (index >= this.dataBits.Length)
            {
                this.growToInclude(el);
            }
            this.dataBits[index] &= ~bitMask(el);
        }

        private void setSize(int nwords)
        {
            long[] destinationArray = new long[nwords];
            int length = Math.Min(nwords, this.dataBits.Length);
            Array.Copy(this.dataBits, 0, destinationArray, 0, length);
            this.dataBits = destinationArray;
        }

        public virtual int size()
        {
            return (this.dataBits.Length << 6);
        }

        public virtual bool subset(BitSet a)
        {
            if (a == null)
            {
                return false;
            }
            return this.and(a).Equals(this);
        }

        public virtual void subtractInPlace(BitSet a)
        {
            if (a != null)
            {
                for (int i = 0; (i < this.dataBits.Length) && (i < a.dataBits.Length); i++)
                {
                    this.dataBits[i] &= ~a.dataBits[i];
                }
            }
        }

        public virtual int[] toArray()
        {
            int[] numArray = new int[this.degree()];
            int num = 0;
            for (int i = 0; i < (this.dataBits.Length << 6); i++)
            {
                if (this.member(i))
                {
                    numArray[num++] = i;
                }
            }
            return numArray;
        }

        public virtual long[] toPackedArray()
        {
            return this.dataBits;
        }

        public override string ToString()
        {
            return this.ToString(",");
        }

        public virtual string ToString(string separator)
        {
            string str = "";
            for (int i = 0; i < (this.dataBits.Length << 6); i++)
            {
                if (this.member(i))
                {
                    if (str.Length > 0)
                    {
                        str = str + separator;
                    }
                    str = str + i;
                }
            }
            return str;
        }

        public virtual string ToString(string separator, ArrayList vocabulary)
        {
            if (vocabulary == null)
            {
                return this.ToString(separator);
            }
            string str = "";
            for (int i = 0; i < (this.dataBits.Length << 6); i++)
            {
                if (this.member(i))
                {
                    object obj2;
                    if (str.Length > 0)
                    {
                        str = str + separator;
                    }
                    if (i >= vocabulary.Count)
                    {
                        obj2 = str;
                        str = string.Concat(new object[] { obj2, "<bad element ", i, ">" });
                    }
                    else if (vocabulary[i] == null)
                    {
                        obj2 = str;
                        str = string.Concat(new object[] { obj2, "<", i, ">" });
                    }
                    else
                    {
                        str = str + ((string) vocabulary[i]);
                    }
                }
            }
            return str;
        }

        public virtual string toStringOfHalfWords()
        {
            string str = new string("".ToCharArray());
            for (int i = 0; i < this.dataBits.Length; i++)
            {
                if (i != 0)
                {
                    str = str + ", ";
                }
                long num2 = this.dataBits[i];
                str = (str + (num2 & ((long) 0xffffffffL)) + "UL") + ", ";
                num2 = SupportClass.URShift(this.dataBits[i], 0x20) & ((long) 0xffffffffL);
                str = str + num2 + "UL";
            }
            return str;
        }

        public virtual string toStringOfWords()
        {
            string str = new string("".ToCharArray());
            for (int i = 0; i < this.dataBits.Length; i++)
            {
                if (i != 0)
                {
                    str = str + ", ";
                }
                str = str + this.dataBits[i] + "L";
            }
            return str;
        }

        private static int wordNumber(int bit)
        {
            return (bit >> 6);
        }
    }
}

