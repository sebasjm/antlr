namespace antlr
{
    using antlr.collections;
    using System;
    using System.Collections;
    using System.IO;
    using System.Text;

    [Serializable]
    public abstract class BaseAST : AST, ICloneable
    {
        protected internal BaseAST down;
        protected internal BaseAST right;
        private static string[] tokenNames = null;
        private static bool verboseStringConversion = false;

        protected BaseAST()
        {
        }

        public virtual void addChild(AST node)
        {
            if (node != null)
            {
                BaseAST down = this.down;
                if (down != null)
                {
                    while (down.right != null)
                    {
                        down = down.right;
                    }
                    down.right = (BaseAST) node;
                }
                else
                {
                    this.down = (BaseAST) node;
                }
            }
        }

        [Obsolete("Deprecated since version 2.7.2. Use ASTFactory.dup() instead.", false)]
        public virtual object Clone()
        {
            return base.MemberwiseClone();
        }

        public static string decode(string text)
        {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < text.Length; i++)
            {
                char ch = text[i];
                if (ch == '&')
                {
                    char ch2 = text[i + 1];
                    char ch3 = text[i + 2];
                    char ch4 = text[i + 3];
                    char ch5 = text[i + 4];
                    char ch6 = text[i + 5];
                    if ((((ch2 == 'a') && (ch3 == 'm')) && (ch4 == 'p')) && (ch5 == ';'))
                    {
                        builder.Append("&");
                        i += 5;
                    }
                    else if (((ch2 == 'l') && (ch3 == 't')) && (ch4 == ';'))
                    {
                        builder.Append("<");
                        i += 4;
                    }
                    else if (((ch2 == 'g') && (ch3 == 't')) && (ch4 == ';'))
                    {
                        builder.Append(">");
                        i += 4;
                    }
                    else if ((((ch2 == 'q') && (ch3 == 'u')) && ((ch4 == 'o') && (ch5 == 't'))) && (ch6 == ';'))
                    {
                        builder.Append("\"");
                        i += 6;
                    }
                    else if ((((ch2 == 'a') && (ch3 == 'p')) && ((ch4 == 'o') && (ch5 == 's'))) && (ch6 == ';'))
                    {
                        builder.Append("'");
                        i += 6;
                    }
                    else
                    {
                        builder.Append("&");
                    }
                }
                else
                {
                    builder.Append(ch);
                }
            }
            return builder.ToString();
        }

        private void doWorkForFindAll(ArrayList v, AST target, bool partialMatch)
        {
            for (AST ast = this; ast != null; ast = ast.getNextSibling())
            {
                if ((partialMatch && ast.EqualsTreePartial(target)) || (!partialMatch && ast.EqualsTree(target)))
                {
                    v.Add(ast);
                }
                if (ast.getFirstChild() != null)
                {
                    ((BaseAST) ast.getFirstChild()).doWorkForFindAll(v, target, partialMatch);
                }
            }
        }

        public static string encode(string text)
        {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < text.Length; i++)
            {
                char ch = text[i];
                switch (ch)
                {
                    case '&':
                    {
                        builder.Append("&amp;");
                        continue;
                    }
                    case '\'':
                    {
                        builder.Append("&apos;");
                        continue;
                    }
                    case '"':
                    {
                        builder.Append("&quot;");
                        continue;
                    }
                    case '<':
                    {
                        builder.Append("&lt;");
                        continue;
                    }
                    case '>':
                    {
                        builder.Append("&gt;");
                        continue;
                    }
                }
                builder.Append(ch);
            }
            return builder.ToString();
        }

        public virtual bool Equals(AST t)
        {
            if (t == null)
            {
                return false;
            }
            return (object.Equals(this.getText(), t.getText()) && (this.Type == t.Type));
        }

        public override bool Equals(object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (base.GetType() != obj.GetType())
            {
                return false;
            }
            return this.Equals((AST) obj);
        }

        public virtual bool EqualsList(AST t)
        {
            if (t == null)
            {
                return false;
            }
            AST ast = this;
            while ((ast != null) && (t != null))
            {
                if (!ast.Equals(t))
                {
                    return false;
                }
                if (ast.getFirstChild() != null)
                {
                    if (!ast.getFirstChild().EqualsList(t.getFirstChild()))
                    {
                        return false;
                    }
                }
                else if (t.getFirstChild() != null)
                {
                    return false;
                }
                ast = ast.getNextSibling();
                t = t.getNextSibling();
            }
            return ((ast == null) && (t == null));
        }

        public virtual bool EqualsListPartial(AST sub)
        {
            if (sub != null)
            {
                AST ast = this;
                while ((ast != null) && (sub != null))
                {
                    if (!ast.Equals(sub))
                    {
                        return false;
                    }
                    if ((ast.getFirstChild() != null) && !ast.getFirstChild().EqualsListPartial(sub.getFirstChild()))
                    {
                        return false;
                    }
                    ast = ast.getNextSibling();
                    sub = sub.getNextSibling();
                }
                if ((ast == null) && (sub != null))
                {
                    return false;
                }
            }
            return true;
        }

        public virtual bool EqualsTree(AST t)
        {
            if (!this.Equals(t))
            {
                return false;
            }
            if (this.getFirstChild() != null)
            {
                if (!this.getFirstChild().EqualsList(t.getFirstChild()))
                {
                    return false;
                }
            }
            else if (t.getFirstChild() != null)
            {
                return false;
            }
            return true;
        }

        public virtual bool EqualsTreePartial(AST sub)
        {
            if (sub != null)
            {
                if (!this.Equals(sub))
                {
                    return false;
                }
                if ((this.getFirstChild() != null) && !this.getFirstChild().EqualsListPartial(sub.getFirstChild()))
                {
                    return false;
                }
            }
            return true;
        }

        public virtual IEnumerator findAll(AST target)
        {
            ArrayList v = new ArrayList(10);
            if (target == null)
            {
                return null;
            }
            this.doWorkForFindAll(v, target, false);
            return v.GetEnumerator();
        }

        public virtual IEnumerator findAllPartial(AST sub)
        {
            ArrayList v = new ArrayList(10);
            if (sub == null)
            {
                return null;
            }
            this.doWorkForFindAll(v, sub, true);
            return v.GetEnumerator();
        }

        public virtual AST getFirstChild()
        {
            return this.down;
        }

        public override int GetHashCode()
        {
            return base.GetHashCode();
        }

        public virtual AST getNextSibling()
        {
            return this.right;
        }

        public int getNumberOfChildren()
        {
            BaseAST down = this.down;
            int num = 0;
            if (down != null)
            {
                num = 1;
                while (down.right != null)
                {
                    down = down.right;
                    num++;
                }
            }
            return num;
        }

        public virtual string getText()
        {
            return "";
        }

        public abstract void initialize(AST t);
        public abstract void initialize(IToken t);
        public abstract void initialize(int t, string txt);
        public virtual void removeChildren()
        {
            this.down = null;
        }

        public virtual void setFirstChild(AST c)
        {
            this.down = (BaseAST) c;
        }

        public virtual void setNextSibling(AST n)
        {
            this.right = (BaseAST) n;
        }

        public virtual void setText(string text)
        {
        }

        public virtual void setType(int ttype)
        {
            this.Type = ttype;
        }

        public static void setVerboseStringConversion(bool verbose, string[] names)
        {
            verboseStringConversion = verbose;
            tokenNames = names;
        }

        public override string ToString()
        {
            StringBuilder builder = new StringBuilder();
            if ((verboseStringConversion && (string.Compare(this.getText(), tokenNames[this.Type], true) != 0)) && (0 != string.Compare(this.getText(), StringUtils.stripFrontBack(tokenNames[this.Type], "\"", "\""), true)))
            {
                builder.Append('[');
                builder.Append(this.getText());
                builder.Append(",<");
                builder.Append(tokenNames[this.Type]);
                builder.Append(">]");
                return builder.ToString();
            }
            return this.getText();
        }

        public virtual string ToStringList()
        {
            AST ast = this;
            string str = "";
            if (ast.getFirstChild() != null)
            {
                str = str + " (";
            }
            str = str + " " + this.ToString();
            if (ast.getFirstChild() != null)
            {
                str = str + ((BaseAST) ast.getFirstChild()).ToStringList();
            }
            if (ast.getFirstChild() != null)
            {
                str = str + " )";
            }
            if (ast.getNextSibling() != null)
            {
                str = str + ((BaseAST) ast.getNextSibling()).ToStringList();
            }
            return str;
        }

        public virtual string ToStringTree()
        {
            AST ast = this;
            string str = "";
            if (ast.getFirstChild() != null)
            {
                str = str + " (";
            }
            str = str + " " + this.ToString();
            if (ast.getFirstChild() != null)
            {
                str = str + ((BaseAST) ast.getFirstChild()).ToStringList();
            }
            if (ast.getFirstChild() != null)
            {
                str = str + " )";
            }
            return str;
        }

        public virtual string ToTree()
        {
            return this.ToTree(string.Empty);
        }

        public virtual string ToTree(string prefix)
        {
            StringBuilder builder = new StringBuilder(prefix);
            if (this.getNextSibling() == null)
            {
                builder.Append("+--");
            }
            else
            {
                builder.Append("|--");
            }
            builder.Append(this.ToString());
            builder.Append(Environment.NewLine);
            if (this.getFirstChild() != null)
            {
                if (this.getNextSibling() == null)
                {
                    builder.Append(((BaseAST) this.getFirstChild()).ToTree(prefix + "   "));
                }
                else
                {
                    builder.Append(((BaseAST) this.getFirstChild()).ToTree(prefix + "|  "));
                }
            }
            if (this.getNextSibling() != null)
            {
                builder.Append(((BaseAST) this.getNextSibling()).ToTree(prefix));
            }
            return builder.ToString();
        }

        public virtual void xmlSerialize(TextWriter outWriter)
        {
            for (AST ast = this; ast != null; ast = ast.getNextSibling())
            {
                if (ast.getFirstChild() == null)
                {
                    ((BaseAST) ast).xmlSerializeNode(outWriter);
                }
                else
                {
                    ((BaseAST) ast).xmlSerializeRootOpen(outWriter);
                    ((BaseAST) ast.getFirstChild()).xmlSerialize(outWriter);
                    ((BaseAST) ast).xmlSerializeRootClose(outWriter);
                }
            }
        }

        public virtual void xmlSerializeNode(TextWriter outWriter)
        {
            StringBuilder builder = new StringBuilder(100);
            builder.Append("<");
            builder.Append(base.GetType().FullName + " ");
            builder.Append(string.Concat(new object[] { "text=\"", encode(this.getText()), "\" type=\"", this.Type, "\"/>" }));
            outWriter.Write(builder.ToString());
        }

        public virtual void xmlSerializeRootClose(TextWriter outWriter)
        {
            outWriter.Write("</" + base.GetType().FullName + ">\n");
        }

        public virtual void xmlSerializeRootOpen(TextWriter outWriter)
        {
            StringBuilder builder = new StringBuilder(100);
            builder.Append("<");
            builder.Append(base.GetType().FullName + " ");
            builder.Append(string.Concat(new object[] { "text=\"", encode(this.getText()), "\" type=\"", this.Type, "\">\n" }));
            outWriter.Write(builder.ToString());
        }

        public virtual int Type
        {
            get
            {
                return 0;
            }
            set
            {
            }
        }
    }
}

