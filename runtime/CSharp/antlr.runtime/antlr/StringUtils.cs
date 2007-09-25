namespace antlr
{
    using System;

    public class StringUtils
    {
        public static string stripBack(string s, char c)
        {
            while ((s.Length > 0) && (s[s.Length - 1] == c))
            {
                s = s.Substring(0, s.Length - 1);
            }
            return s;
        }

        public static string stripBack(string s, string remove)
        {
            bool flag;
            do
            {
                flag = false;
                for (int i = 0; i < remove.Length; i++)
                {
                    char ch = remove[i];
                    while ((s.Length > 0) && (s[s.Length - 1] == ch))
                    {
                        flag = true;
                        s = s.Substring(0, s.Length - 1);
                    }
                }
            }
            while (flag);
            return s;
        }

        public static string stripFront(string s, char c)
        {
            while ((s.Length > 0) && (s[0] == c))
            {
                s = s.Substring(1);
            }
            return s;
        }

        public static string stripFront(string s, string remove)
        {
            bool flag;
            do
            {
                flag = false;
                for (int i = 0; i < remove.Length; i++)
                {
                    char ch = remove[i];
                    while ((s.Length > 0) && (s[0] == ch))
                    {
                        flag = true;
                        s = s.Substring(1);
                    }
                }
            }
            while (flag);
            return s;
        }

        public static string stripFrontBack(string src, string head, string tail)
        {
            int index = src.IndexOf(head);
            int num2 = src.LastIndexOf(tail);
            if ((index == -1) || (num2 == -1))
            {
                return src;
            }
            return src.Substring(index + 1, num2 - (index + 1));
        }
    }
}

