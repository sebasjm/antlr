namespace antlr
{
    using antlr.collections;
    using antlr.collections.impl;
    using System;
    using System.Collections;
    using System.Diagnostics;
    using System.Reflection;

    public class ASTFactory
    {
        protected Type defaultASTNodeTypeObject_;
        protected ASTNodeCreator defaultCreator_;
        protected FactoryEntry[] heteroList_;
        protected Hashtable typename2creator_;

        public ASTFactory() : this(typeof(CommonAST))
        {
        }

        public ASTFactory(string nodeTypeName) : this(loadNodeTypeObject(nodeTypeName))
        {
        }

        public ASTFactory(Type nodeType)
        {
            this.heteroList_ = new FactoryEntry[5];
            this.defaultASTNodeTypeObject_ = nodeType;
            this.defaultCreator_ = null;
            this.typename2creator_ = new Hashtable(0x20, 0.3f);
            this.typename2creator_["antlr.CommonAST"] = CommonAST.Creator;
            this.typename2creator_["antlr.CommonASTWithHiddenTokens"] = CommonASTWithHiddenTokens.Creator;
        }

        public virtual void addASTChild(ref ASTPair currentAST, AST child)
        {
            if (child != null)
            {
                if (currentAST.root == null)
                {
                    currentAST.root = child;
                }
                else if (currentAST.child == null)
                {
                    currentAST.root.setFirstChild(child);
                }
                else
                {
                    currentAST.child.setNextSibling(child);
                }
                currentAST.child = child;
                currentAST.advanceChildToEnd();
            }
        }

        public virtual AST create()
        {
            if (this.defaultCreator_ == null)
            {
                return this.createFromNodeTypeObject(this.defaultASTNodeTypeObject_);
            }
            return this.defaultCreator_.Create();
        }

        public virtual AST create(AST aNode)
        {
            if (aNode == null)
            {
                return null;
            }
            AST ast = this.createFromAST(aNode);
            ast.initialize(aNode);
            return ast;
        }

        public virtual AST create(IToken tok)
        {
            if (tok == null)
            {
                return null;
            }
            AST ast = this.createFromNodeType(tok.Type);
            ast.initialize(tok);
            return ast;
        }

        public virtual AST create(int type)
        {
            AST ast = this.createFromNodeType(type);
            ast.initialize(type, "");
            return ast;
        }

        public virtual AST create(IToken tok, string ASTNodeTypeName)
        {
            AST ast = this.createFromNodeName(ASTNodeTypeName);
            ast.initialize(tok);
            return ast;
        }

        public virtual AST create(int type, string txt)
        {
            AST ast = this.createFromNodeType(type);
            ast.initialize(type, txt);
            return ast;
        }

        public virtual AST create(int type, string txt, string ASTNodeTypeName)
        {
            AST ast = this.createFromNodeName(ASTNodeTypeName);
            ast.initialize(type, txt);
            return ast;
        }

        private AST createFromAST(AST node)
        {
            AST ast = null;
            Type nodeTypeObject = node.GetType();
            ASTNodeCreator creator = (ASTNodeCreator) this.typename2creator_[nodeTypeObject.FullName];
            if (creator != null)
            {
                ast = creator.Create();
                if (ast == null)
                {
                    throw new ArgumentException("Unable to create AST Node Type: '" + nodeTypeObject.FullName + "'");
                }
                return ast;
            }
            return this.createFromNodeTypeObject(nodeTypeObject);
        }

        private AST createFromNodeName(string nodeTypeName)
        {
            AST ast = null;
            ASTNodeCreator creator = (ASTNodeCreator) this.typename2creator_[nodeTypeName];
            if (creator != null)
            {
                ast = creator.Create();
                if (ast == null)
                {
                    throw new ArgumentException("Unable to create AST Node Type: '" + nodeTypeName + "'");
                }
                return ast;
            }
            return this.createFromNodeTypeObject(loadNodeTypeObject(nodeTypeName));
        }

        private AST createFromNodeType(int nodeTypeIndex)
        {
            Debug.Assert((nodeTypeIndex >= 0) && (nodeTypeIndex <= this.heteroList_.Length), "Invalid AST node type!");
            FactoryEntry entry = this.heteroList_[nodeTypeIndex];
            if ((entry != null) && (entry.Creator != null))
            {
                return entry.Creator.Create();
            }
            if ((entry == null) || (entry.NodeTypeObject == null))
            {
                if (this.defaultCreator_ == null)
                {
                    return this.createFromNodeTypeObject(this.defaultASTNodeTypeObject_);
                }
                return this.defaultCreator_.Create();
            }
            return this.createFromNodeTypeObject(entry.NodeTypeObject);
        }

        private AST createFromNodeTypeObject(Type nodeTypeObject)
        {
            AST ast = null;
            try
            {
                ast = (AST) Activator.CreateInstance(nodeTypeObject);
                if (ast == null)
                {
                    throw new ArgumentException("Unable to create AST Node Type: '" + nodeTypeObject.FullName + "'");
                }
            }
            catch (Exception exception)
            {
                throw new ArgumentException("Unable to create AST Node Type: '" + nodeTypeObject.FullName + "'", exception);
            }
            return ast;
        }

        public virtual AST dup(AST t)
        {
            if (t == null)
            {
                return null;
            }
            AST ast = this.createFromAST(t);
            ast.initialize(t);
            return ast;
        }

        public virtual AST dupList(AST t)
        {
            AST ast = this.dupTree(t);
            for (AST ast2 = ast; t != null; ast2 = ast2.getNextSibling())
            {
                t = t.getNextSibling();
                ast2.setNextSibling(this.dupTree(t));
            }
            return ast;
        }

        public virtual AST dupTree(AST t)
        {
            AST ast = this.dup(t);
            if (t != null)
            {
                ast.setFirstChild(this.dupList(t.getFirstChild()));
            }
            return ast;
        }

        public virtual void error(string e)
        {
            Console.Error.WriteLine(e);
        }

        private static Type loadNodeTypeObject(string nodeTypeName)
        {
            Type type = null;
            bool flag = false;
            if (nodeTypeName != null)
            {
                foreach (Assembly assembly in AppDomain.CurrentDomain.GetAssemblies())
                {
                    try
                    {
                        type = assembly.GetType(nodeTypeName);
                        if (type != null)
                        {
                            flag = true;
                            break;
                        }
                    }
                    catch
                    {
                        flag = false;
                    }
                }
            }
            if (!flag)
            {
                throw new TypeLoadException("Unable to load AST Node Type: '" + nodeTypeName + "'");
            }
            return type;
        }

        public virtual AST make(params AST[] nodes)
        {
            if ((nodes == null) || (nodes.Length == 0))
            {
                return null;
            }
            AST ast = nodes[0];
            AST ast2 = null;
            if (ast != null)
            {
                ast.setFirstChild(null);
            }
            for (int i = 1; i < nodes.Length; i++)
            {
                if (nodes[i] != null)
                {
                    if (ast == null)
                    {
                        ast = ast2 = nodes[i];
                    }
                    else if (ast2 == null)
                    {
                        ast.setFirstChild(nodes[i]);
                        ast2 = ast.getFirstChild();
                    }
                    else
                    {
                        ast2.setNextSibling(nodes[i]);
                        ast2 = ast2.getNextSibling();
                    }
                    while (ast2.getNextSibling() != null)
                    {
                        ast2 = ast2.getNextSibling();
                    }
                }
            }
            return ast;
        }

        public virtual AST make(ASTArray nodes)
        {
            return this.make(nodes.array);
        }

        public virtual void makeASTRoot(ref ASTPair currentAST, AST root)
        {
            if (root != null)
            {
                root.addChild(currentAST.root);
                currentAST.child = currentAST.root;
                currentAST.advanceChildToEnd();
                currentAST.root = root;
            }
        }

        [Obsolete("Replaced by setTokenTypeASTNodeType(int, string) since version 2.7.2.6", true)]
        public void registerFactory(int NodeType, string NodeTypeName)
        {
            this.setTokenTypeASTNodeType(NodeType, NodeTypeName);
        }

        public void setASTNodeCreator(ASTNodeCreator creator)
        {
            this.defaultCreator_ = creator;
        }

        public virtual void setASTNodeType(string t)
        {
            if ((this.defaultCreator_ != null) && (t != this.defaultCreator_.ASTNodeTypeName))
            {
                this.defaultCreator_ = null;
            }
            this.defaultASTNodeTypeObject_ = loadNodeTypeObject(t);
        }

        public void setMaxNodeType(int NodeType)
        {
            if (this.heteroList_ == null)
            {
                this.heteroList_ = new FactoryEntry[NodeType + 1];
            }
            else
            {
                FactoryEntry[] entryArray;
                int length = this.heteroList_.Length;
                if (NodeType >= length)
                {
                    entryArray = new FactoryEntry[NodeType + 1];
                    Array.Copy(this.heteroList_, 0, entryArray, 0, length);
                    this.heteroList_ = entryArray;
                }
                else if (NodeType < length)
                {
                    entryArray = new FactoryEntry[NodeType + 1];
                    Array.Copy(this.heteroList_, 0, entryArray, 0, NodeType + 1);
                    this.heteroList_ = entryArray;
                }
            }
        }

        public void setTokenTypeASTNodeCreator(int NodeType, ASTNodeCreator creator)
        {
            if (NodeType < 4)
            {
                throw new ANTLRException("Internal parser error: Cannot change AST Node Type for Token ID '" + NodeType + "'");
            }
            if (NodeType > (this.heteroList_.Length + 1))
            {
                this.setMaxNodeType(NodeType);
            }
            if (this.heteroList_[NodeType] == null)
            {
                this.heteroList_[NodeType] = new FactoryEntry(creator);
            }
            else
            {
                this.heteroList_[NodeType].Creator = creator;
            }
            this.typename2creator_[creator.ASTNodeTypeName] = creator;
        }

        public void setTokenTypeASTNodeType(int tokenType, string NodeTypeName)
        {
            if (tokenType < 4)
            {
                throw new ANTLRException("Internal parser error: Cannot change AST Node Type for Token ID '" + tokenType + "'");
            }
            if (tokenType > (this.heteroList_.Length + 1))
            {
                this.setMaxNodeType(tokenType);
            }
            if (this.heteroList_[tokenType] == null)
            {
                this.heteroList_[tokenType] = new FactoryEntry(loadNodeTypeObject(NodeTypeName));
            }
            else
            {
                this.heteroList_[tokenType].NodeTypeObject = loadNodeTypeObject(NodeTypeName);
            }
        }

        protected class FactoryEntry
        {
            public ASTNodeCreator Creator;
            public Type NodeTypeObject;

            public FactoryEntry(ASTNodeCreator creator)
            {
                this.Creator = creator;
            }

            public FactoryEntry(Type typeObj)
            {
                this.NodeTypeObject = typeObj;
            }

            public FactoryEntry(Type typeObj, ASTNodeCreator creator)
            {
                this.NodeTypeObject = typeObj;
                this.Creator = creator;
            }
        }
    }
}

