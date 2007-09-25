namespace Antlr.Runtime.Collections
{
    using System;
    using System.Collections;
    using System.Text;

    public class CollectionUtils
    {
        public static string DictionaryToString(IDictionary dict)
        {
            StringBuilder builder = new StringBuilder();
            if (dict != null)
            {
                builder.Append("{");
                int num = 0;
                foreach (DictionaryEntry entry in dict)
                {
                    if (num > 0)
                    {
                        builder.Append(", ");
                    }
                    builder.AppendFormat("{0}={1}", entry.Key.ToString(), entry.Value.ToString());
                    num++;
                }
                builder.Append("}");
            }
            else
            {
                builder.Insert(0, "null");
            }
            return builder.ToString();
        }

        public static string ListToString(IList coll)
        {
            StringBuilder builder = new StringBuilder();
            if (coll != null)
            {
                builder.Append("[");
                for (int i = 0; i < coll.Count; i++)
                {
                    if (i > 0)
                    {
                        builder.Append(", ");
                    }
                    if (coll[i] == null)
                    {
                        builder.Append("null");
                    }
                    else
                    {
                        builder.Append(coll[i].ToString());
                    }
                }
                builder.Append("]");
            }
            else
            {
                builder.Insert(0, "null");
            }
            return builder.ToString();
        }
    }
}

