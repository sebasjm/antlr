"""ANTLR3 runtime package, tree module"""

# [The "BSD licence"]
# Copyright (c) 2005-2006 Terence Parr
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
# 1. Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright
#    notice, this list of conditions and the following disclaimer in the
#    documentation and/or other materials provided with the distribution.
# 3. The name of the author may not be used to endorse or promote products
#    derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
# IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
# OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
# IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
# INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
# NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
# THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

import warnings
from copy import deepcopy

from antlr3.constants import UP, DOWN, EOF
from antlr3.recognizers import BaseRecognizer
#from antlr3.streams import IntStream
from antlr3.tokens import CommonToken, Token, INVALID_TOKEN

############################################################################
#
# tree related exceptions
#
############################################################################


class RewriteCardinalityException(RuntimeError):
    """Base class for all exceptions thrown during AST rewrite construction.

    This signifies a case where the cardinality of two or more elements
    in a subrule are different: (ID INT)+ where |ID|!=|INT|
    """

    def __init__(self, elementDescription):
        RuntimeError.__init__(self, elementDescription)

        self.elementDescription = elementDescription


    def getMessage(self):
        return self.elementDescription


class RewriteEarlyExitException(RewriteCardinalityException):
    """No elements within a (...)+ in a rewrite rule"""

    def __init__(self, elementDescription=None):
        RewriteCardinalityException.__init__(self, elementDescription)


class RewriteEmptyStreamException(RewriteCardinalityException):
    """
    Ref to ID or expr but no tokens in ID stream or subtrees in expr stream
    """

    pass


############################################################################
#
# basic Tree and TreeAdaptor interfaces
#
############################################################################


# What does a tree look like?  ANTLR has a number of support classes
# such as CommonTreeNodeStream that work on these kinds of trees.  You
# don't have to make your trees implement this interface, but if you do,
# you'll be able to use more support code.

# NOTE: When constructing trees, ANTLR can build any kind of tree; it can
# even use Token objects as trees if you add a child list to your tokens.

# This is a tree node without any payload; just navigation and factory stuff.


class Tree(object):
    """Base tree interface."""
    def getChild(self, i):
        raise NotImplementedError
    

    def getChildCount(self):
        raise NotImplementedError
    

    def addChild(self, t):
        """Add t as a child to this node.  If t is null, do nothing.  If t
        is nil, add all children of t to this' children.
        @param t
        """

        raise NotImplementedError
    

    def isNil(self):
        """Indicates the node is a nil node but may still have children, meaning
        the tree is a flat list.
        """

        raise NotImplementedError
    

    def dupTree(self):
        raise NotImplementedError
    

    def dupNode(self):
        raise NotImplementedError
    

    def getType(self):
        """Return a token type; needed for tree parsing."""

        raise NotImplementedError
    

    def getText(self):
        raise NotImplementedError
    

    def getLine(self):
        """In case we don't have a token payload, what is the line for errors?"""

        raise NotImplementedError
    

    def getCharPositionInLine(self):
        raise NotImplementedError


class TreeAdaptor(object):
    """
    How to create and navigate trees.  Rather than have a separate factory
    and adaptor, I've merged them.  Makes sense to encapsulate.

    This takes the place of the tree construction code generated in the
    generated code in 2.x and the ASTFactory.

    I do not need to know the type of a tree at all so they are all
    generic Objects.  This may increase the amount of typecasting needed. :(
    """
    
    ## C o n s t r u c t i o n

    def createWithPayload(self, payload):
        """
        Create a tree node from Token object; for CommonTree type trees,
        then the token just becomes the payload.  This is the most
        common create call.
        """

        raise NotImplementedError
    

    def dupTree(self, tree):
        """Duplicate tree recursively, using dupNode() for each node"""

        raise NotImplementedError


    def dupNode(self, treeNode):
        """Duplicate a single tree node"""

        raise NotImplementedError


    def nil(self):
        """
        Return a nil node (an empty but non-null node) that can hold
        a list of element as the children.  If you want a flat tree (a list)
        use "t=adaptor.nil(); t.addChild(x); t.addChild(y);"
	"""

        raise NotImplementedError


    def isNil(self, tree):
        """Is tree considered a nil node used to make lists of child nodes?"""

        raise NotImplementedError


    def addChild(self, t, child):
        """
        Add a child to the tree t.  If child is a flat tree (a list), make all
        in list children of t.  Warning: if t has no children, but child does
        and child isNil then you can decide it is ok to move children to t via
        t.children = child.children; i.e., without copying the array.  Just
        make sure that this is consistent with have the user will build
        ASTs.
	"""

        raise NotImplementedError


    def becomeRoot(self, newRoot, oldRoot):
        """
        If oldRoot is a nil root, just copy or move the children to newRoot.
        If not a nil root, make oldRoot a child of newRoot.
	
	   old=^(nil a b c), new=r yields ^(r a b c)
	   old=^(a b c), new=r yields ^(r ^(a b c))

        If newRoot is a nil-rooted single child tree, use the single
        child as the new root node.

           old=^(nil a b c), new=^(nil r) yields ^(r a b c)
	   old=^(a b c), new=^(nil r) yields ^(r ^(a b c))

        If oldRoot was null, it's ok, just return newRoot (even if isNil).

           old=null, new=r yields r
	   old=null, new=^(nil r) yields ^(nil r)

        Return newRoot.  Throw an exception if newRoot is not a
        simple node or nil root with a single child node--it must be a root
        node.  If newRoot is ^(nil x) return x as newRoot.

        Be advised that it's ok for newRoot to point at oldRoot's
        children; i.e., you don't have to copy the list.  We are
        constructing these nodes so we should have this control for
        efficiency.
        """

        raise NotImplementedError


    def rulePostProcessing(self, root):
        """
        Given the root of the subtree created for this rule, post process
        it to do any simplifications or whatever you want.  A required
        behavior is to convert ^(nil singleSubtree) to singleSubtree
        as the setting of start/stop indexes relies on a single non-nil root
        for non-flat trees.

        Flat trees such as for lists like "idlist : ID+ ;" are left alone
        unless there is only one ID.  For a list, the start/stop indexes
        are set in the nil node.

        This method is executed after all rule tree construction and right
        before setTokenBoundaries().
        """

        raise NotImplementedError


    def getUniqueID(self, node):
        """For identifying trees.

        How to identify nodes so we can say "add node to a prior node"?
        Even becomeRoot is an issue.  Use System.identityHashCode(node)
        usually.
        """

        raise NotImplementedError


    # R e w r i t e  R u l e s

    def becomeRoot(self, newRoot, oldRoot):
        """Create a node for newRoot make it the root of oldRoot.

        If oldRoot is a nil root, just copy or move the children to newRoot.
        If not a nil root, make oldRoot a child of newRoot.

        Return node created for newRoot.

        Be advised: when debugging ASTs, the DebugTreeAdaptor manually
        calls create(Token child) and then plain becomeRoot(node, node)
        because it needs to trap calls to create, but it can't since
        it delegates to not inherits from the TreeAdaptor.
        """

        raise NotImplementedError


    def createFromToken(self, tokenType, fromToken, text=None):
        """
        Create a new node derived from a token, with a new token type and
        (optionally) new text.

        This is invoked from an imaginary node ref on right side of a
        rewrite rule as IMAG[$tokenLabel] or IMAG[$tokenLabel "IMAG"].

        This should invoke createToken(Token).
        """

        raise NotImplementedError


    def createFromType(self, tokenType, text):
        """Create a new node derived from a token, with a new token type.

        This is invoked from an imaginary node ref on right side of a
        rewrite rule as IMAG["IMAG"].

        This should invoke createToken(int,String).
	"""

        raise NotImplementedError


    # C o n t e n t

    def getType(self, t):
        """For tree parsing, I need to know the token type of a node"""

        raise NotImplementedError


    def setType(self, t, type):
        """Node constructors can set the type of a node"""

        raise NotImplementedError


    def getText(self, t):
        raise NotImplementedError

    def setText(self, t, text):
        """Node constructors can set the text of a node"""

        raise NotImplementedError


    def getToken(self, t):
        """Return the token object from which this node was created.

        Currently used only for printing an error message.
        The error display routine in BaseRecognizer needs to
        display where the input the error occurred. If your
        tree of limitation does not store information that can
        lead you to the token, you can create a token filled with
        the appropriate information and pass that back.  See
        BaseRecognizer.getErrorMessage().
	"""

        raise NotImplementedError


    def setTokenBoundaries(self, t, startToken, stopToken):
        """
        Where are the bounds in the input token stream for this node and
        all children?  Each rule that creates AST nodes will call this
        method right before returning.  Flat trees (i.e., lists) will
        still usually have a nil root node just to hold the children list.
        That node would contain the start/stop indexes then.
	"""

        raise NotImplementedError


    def getTokenStartIndex(self, t):
        """
        Get the token start index for this subtree; return -1 if no such index
        """

        raise NotImplementedError

        
    def getTokenStopIndex(self, t):
        """
        Get the token stop index for this subtree; return -1 if no such index
        """

        raise NotImplementedError
        

    # N a v i g a t i o n  /  T r e e  P a r s i n g

    def getChild(self, t, i):
        """Get a child 0..n-1 node"""

        raise NotImplementedError


    def getChildCount(self, t):
        """How many children?  If 0, then this is a leaf node"""

        raise NotImplementedError


    ## Misc

    def create(self, *args):
        """
        Deprecated, use createWithPayload, createFromToken or createFromType.

        This method only exists to mimic the Java interface of TreeAdaptor.
        
        """

        if len(args) == 1 and isinstance(args[0], Token):
            # Object create(Token payload);
            warnings.warn(
                "Using create() is deprecated, use createWithPayload()",
                DeprecationWarning,
                stacklevel=2
                )
            return self.createWithPayload(args[0])

        if len(args) == 2 and isinstance(args[0], (int, long)) and isinstance(args[1], Token):
            # Object create(int tokenType, Token fromToken);
            warnings.warn(
                "Using create() is deprecated, use createFromToken()",
                DeprecationWarning,
                stacklevel=2
                )
            return self.createFromToken(args[0], args[1])

        if len(args) == 3 and isinstance(args[0], (int, long)) and isinstance(args[1], Token) and isinstance(args[2], basestring):
            # Object create(int tokenType, Token fromToken, String text);
            warnings.warn(
                "Using create() is deprecated, use createFromToken()",
                DeprecationWarning,
                stacklevel=2
                )
            return self.createFromToken(args[0], args[1], args[2])

        if len(args) == 2 and isinstance(args[0], (int, long)) and isinstance(args[1], basestring):
            # Object create(int tokenType, String text);
            warnings.warn(
                "Using create() is deprecated, use createFromType()",
                DeprecationWarning,
                stacklevel=2
                )
            return self.createFromType(args[0], args[1])

        raise TypeError(
            "No create method with this signature found: %s"
            % (', '.join(type(v).__name__ for v in args))
            )
    

# A generic tree implementation with no payload.  You must subclass to
# actually have any user data.  ANTLR v3 uses a list of children approach
# instead of the child-sibling approach in v2.  A flat tree (a list) is
# an empty node whose children represent the list.  An empty, but
# non-null node is called "nil".

class BaseTree(Tree):
    def __init__(self):
        Tree.__init__(self)
        self.children = []

    def getChild(self, i):
        if i >= len(self.children):
            return None

        return self.children[i]

    def getFirstChildWithType(self, treeType):
        for child in self.children:
            if child.getType() == treeType:
                return child

        return None

    def getChildCount(self):
        if self.children:
            return len(self.children)

        else:
            return 0

    # Warning: if t has no children, but child does
    # and child isNil then it is ok to move children to t via
    # t.children = child.children; i.e., without copying the array.  This
    # is for construction and I'm not sure it's completely general for
    # a tree's addChild method to work this way.
    #
    def addChild(self, t):
        if isinstance(t, Token):
            t = CommonTree(t)

        if t.isNil():
            self.children += t.children

        else:
            self.children.append(t)

    # Add all elements of kids list as children of this node
    def addChildren(self, children):
        self.children += children

    def setChild(self, i, t):
        self.children[i] = t

    def deleteChild(self, i):
        del self.children[i]

    def isNil(self):
        return False

    def dupTree(self):
        return deepcopy(self)

    def __str__(self):
        result = '('

        for child in self.children:
            result += ' ' + str(child)

        result += ')'

        return result

    def getLine(self):
        return 0

    def getCharPositionInLine(self):
        return 0



class BaseTreeAdaptor(TreeAdaptor):
    # If oldRoot is a nil root, just copy or move the children to newRoot.
    # If not a nil root, make oldRoot a child of newRoot.

    #   old=^(nil a b c), new=r yields ^(r a b c)
    #   old=^(a b c), new=r yields ^(r ^(a b c))
    
    # If newRoot is a nil-rooted single child tree, use the single
    # child as the new root node.

    #   old=^(nil a b c), new=^(nil r) yields ^(r a b c)
    #   old=^(a b c), new=^(nil r) yields ^(r ^(a b c))

    # If oldRoot was null, it's ok, just return newRoot (even if isNil).

    #   old=null, new=r yields r
    #   old=null, new=^(nil r) yields ^(nil r)
    
    # Return newRoot.  Throw an exception if newRoot is not a
    # simple node or nil root with a single child node--it must be a root
    # node.  If newRoot is ^(nil x) return x as newRoot.

    # Be advised that it's ok for newRoot to point at oldRoot's
    # children; i.e., you don't have to copy the list.  We are
    # constructing these nodes so we should have this control for
    # efficiency.
    def becomeRoot(self, newRoot, oldRoot):
        if not isinstance(newRoot, CommonTree):
            newRoot = self.createWithPayload(newRoot)

        # handle ^(nil real-node)
        if newRoot.isNil():
            if newRoot.getChildCount() > 1:
                # TODO: make tree run time exceptions hierarchy
                raise "more than one node as root (TODO: make exception hierarchy)"

            newRoot = newRoot.getChild(0)
    
        # add oldRoot to newRoot; addChild takes care of case where oldRoot
        # is a flat list (i.e., nil-rooted tree).  All children of oldRoot
        # are added to newRoot.
        newRoot.addChild(oldRoot)
        return newRoot

    # Transform ^(nil x) to x #
    def rulePostProcessing(self, r):
        #print ">" + repr(r)
        if r.isNil() and r.getChildCount() == 1:
            r = r.getChild(0)

        #print "<" + str(r)
        return r

    def addChild(self, tree, child):
        if isinstance(child, Token):
            child = self.createWithPayload(child)

        return tree.addChild(child)

    def createFromToken(self, tokenType, fromToken, text=None):
        fromToken = self.createToken(fromToken)
        fromToken.type = tokenType
        if text is not None:
            fromToken.text = text
        t = self.createWithPayload(fromToken)
        return t


    def createFromType(self, tokenType, text):
        fromToken = self.createToken(tokenType=tokenType, text=text)
        t = self.createWithPayload(fromToken)
        return t

    def nil(self):
        return self.createWithPayload(None)


import traceback

# A tree node that is wrapper for a Token object. #
class CommonTree(BaseTree):
    # What token indexes bracket all tokens associated with this node
    # and below?
    def __init__(self, token=None):
        BaseTree.__init__(self)

        if isinstance(token, CommonTree):
            self.token = token.token
        else:
            self.token = token

        assert self.token is None or 'text' in dir(self.token), self.token
        #assert hasattr(self.token, 'text'), (repr(self.token), repr(token))
        
        self.startIndex = -1
        self.stopIndex = -1

        
    def dupNode(self):
        return CommonTree(self)


    def getToken(self):
        return self.token

    def isNil(self):
        return self.token is None

    def getType(self):
        if self.token is None:
            return 0

        else:
            return self.token.getType()

    def getLine(self):
        if self.token is None or self.token.getLine() == 0:
            if len(self.getChildCount()) > 0:
                return self.getChild(0).getLine()
            else:
                return 0

        return self.token.getLine()

    def getCharPositionInLine(self):
        if self.token is None or self.token.getCharPositionInLine() == -1:
            if self.getChildCount():
                return self.getChild(0).getCharPositionInLine()
            else:
                return 0

        else:
            return self.token.getCharPositionInLine()

    def __str__(self):
        if self.isNil():
            return "nil"

        else:
            return self.token.text

    def toStringTree(self):
        if not self.children:
            return str(self)

        ret = ''
        if not self.isNil():
            ret += '(%s ' % (str(self))
        
        ret += ' '.join([child.toStringTree() for child in self.children])

        if not self.isNil():
            ret += ')'

        return ret

    def getCurrentDetails(self, e):
        e.token = self.node.token
        e.line = token.getLine()
        e.charPositionInLine = token.getCharPositionInLine()
        
# A TreeAdaptor that works with any Tree implementation.  It provides
# really just factory methods; all the work is done by BaseTreeAdaptor.
# If you would like to have different tokens created than ClassicToken
# objects, you need to override this and then set the parser tree adaptor to
# use your subclass.

# To get your parser to build nodes of a different type, override
# create(Token).
 #
class CommonTreeAdaptor(BaseTreeAdaptor):
    # Duplicate a node.  This is part of the factory;
    #	override if you want another kind of node to be built.
    def dupNode(self, treeNode):
        return treeNode.dupNode()

    def createToken(self, fromToken=None, tokenType=None, text=None):
        if fromToken is not None:
            return CommonToken(oldToken=fromToken)

        return CommonToken(type=tokenType, text=text)


    # Track start/stop token for subtree root created for a rule.
    # Only works with CommonTree nodes.  For rules that match nothing,
    # seems like this will yield start=i and stop=i-1 in a nil node.
    # Might be useful info so I'll not force to be i..i.
    def setTokenBoundaries(self, t, startToken, stopToken):
        if t is None:
            return

        start = 0
        stop = 0
        
        if startToken:
            start = startToken.index
                
        if stopToken:
            stop = stopToken.index

        t.startIndex = start
        t.stopIndex = stop

    def getTokenStartIndex(self, t):
        return t.startIndex

    def getTokenStopIndex(self, t):
        return t.stopIndex

    def getText(self, t):
        return t.getText()

    def getType(self, t):
        return t.getType()


    def createWithPayload(self, payload):
        return CommonTree(payload)


# A stream of tree nodes, accessing nodes from a tree of some kind #
class TreeNodeStream(object): #IntStream):
    def LT(self, k):
        raise NotImplementedError

    def getTreeSource(self):
        raise NotImplementedError

    def getTreeAdaptor(self):
        raise NotImplementedError

    def setUniqueNavigationNodes(self, uniqueNavigationNodes):
        raise NotImplementedError

    def toString(self, start, stop):
        raise NotImplementedError


# A stream of tree nodes, accessing nodes from a tree of some kind.
# The stream can be accessed as an Iterator or via LT(1)/consume or
# LT(i).  No new nodes should be created during the walk.  A small buffer
# of tokens is kept to efficiently and easily handle LT(i) calls, though
# the lookahead mechanism is fairly complicated.

# For tree rewriting during tree parsing, this must also be able
# to replace a set of children without "losing its place".
# That part is not yet implemented.  Will permit a rule to return
# a different tree and have it stitched into the output tree probably.

# Because this class implements Iterator you can walk a tree with
# a for loop looking for nodes.  When using the Iterator
# interface methods you do not get DOWN and UP imaginary nodes that
# are used for parsing via TreeNodeStream interface.
 #
class CommonTreeNodeStream(TreeNodeStream):

    class DummyTree(BaseTree):
        def dupNode(self):
            return None

    # all these navigation nodes are shared and hence they
    # cannot contain any line/column info

    class NavDownNode(DummyTree):
        def getType(self):
            return DOWN
        
        def __str__(self):
            return 'DOWN'

    class NavUpNode(DummyTree):
        def getType(self):
            return UP
        
        def __str__(self):
            return 'UP'
	
    class EOFNode(DummyTree):
        def getType(self):
            return EOF
        
        def __str__(self):
            return 'EOF'
	
    DOWN = NavDownNode()
    
    UP = NavUpNode()

    EOF_NODE = EOFNode()

    # When walking ahead with cyclic DFA or for syntactic predicates,
    # we need to record the state of the tree node stream.  This
    # class wraps up the current state of the CommonTreeNodeStream.
    # Calling mark() will push another of these on the markers stack.
    class TreeWalkState(object):
        def __init__(self):
            self.currentChildIndex = 0
            self.absoluteNodeIndex = 0
            self.currentNode = None
            self.previousNode = None
            # Record state of the nodeStack #
            self.nodeStackSize = 0
            # Record state of the indexStack #
            self.indexStackSize = 0
            self.lookahead = []

    def __init__(self, adapter, tree):

        # Reuse same DOWN, UP navigation nodes unless this is true #
        self.uniqueNavigationNodes = False
        
        # Pull nodes from which tree? #
        self.root = None

        # What tree adaptor was used to build these trees 
        self.adaptor = None

        # As we walk down the nodes, we must track parent nodes so we know
        # where to go after walking the last child of a node.  When visiting
        # a child, push current node and current index.
        self.nodeStack = []

        # Track which child index you are visiting for each node we push.
        # TODO: pretty inefficient...use int[] when you have time
        self.indexStack = []

        # Track the last mark() call result value for use in rewind(). #
        self.lastMarker = None

        # Which node are we currently visiting? #
        self.currentNode = None

        # Which node did we visit last?  Used for LT(-1) calls. #
        self.previousNode = None

        # Which child are we currently visiting?  If -1 we have not visited
        # this node yet; next consume() request will set currentIndex to 0.
        self.currentChildIndex = None

        # What node index did we just consume?  i=0..n-1 for n node trees.
        # IntStream.next is hence 1 + this value.  Size will be same.
        self.absoluteNodeIndex = 0
        
        # Buffer tree node stream for use with LT(i).  This list grows
        # to fit lookahead depths, but consume() wraps like a circular
        # buffer.
        self.lookahead = []
            
        # Calls to mark() may be nested so we have to track a stack of
        # them.  The marker is an index into this stack.  Index 0 is
        # the first marker.  This is a List<TreeWalkState>
        self.markers = []

        self.root = tree
        self.adaptor = adaptor
        reset()

    def reset():
        currentNode = root
        previousNode = None
        currentChildIndex = -1
        absoluteNodeIndex = -1
        head = tail = 0

    # Satisfy TreeNodeStream
    
    # Get tree node at current input pointer + i ahead where i=1 is next node.
    # i<0 indicates nodes in the past.  So -1 is previous node and -2 is
    # two nodes ago. LT(0) is undefined.  For i>=n, return None.
    # Return None for LT(0) and any index that results in an absolute address
    # that is negative.
    
    # This is analogus to the LT() method of the TokenStream, but this
    # returns a tree node instead of a token.  Makes code gen identical
    # for both parser and tree grammars. :)
    def LT(self, k):
        if k == -1:
            return previousNode

        if k < 0:
            raise IllegalArgumentException('tree node streams cannot look backwards more than 1 node')
        if k == 0:
            return INVALID_NODE
        
        self.fill(k)
        return lookahead[k - 1]

    # Where is this stream pulling nodes from?  This is not the name, but
    # the object that provides node objects.
    def getTreeSource(self):
        return self.root

    # Make sure we have at least k symbols in lookahead buffer #
    def fill(self, k):
        while len(self.lookahead) < k:
            self.next() 

    # Add a node to the lookahead buffer.  Add at lookahead[tail].
    # If you tail+1 == head, then we must create a bigger buffer
    # and copy all the nodes over plus reset head, tail.  After
    # this method, LT(1) will be lookahead[0].
    def addLookahead(self, node):
        self.lookahead.append(node)

    def consume(self):
        # make sure there is something in lookahead buf, which might call next()
        self.fill(1)
        self.absoluteNodeIndex += 1
        del self.lookahead[0]

    def LA(self, i):
        t = LT(i)
        if not t:
            return INVALID_TOKEN_TYPE

        return t.getType()

    # Record the current state of the tree walk which includes
    # the current node and stack state.
    def mark(self):
        state = TreeWalkState()
        state.absoluteNodeIndex = self.absoluteNodeIndex
        state.currentChildIndex = self.currentChildIndex
        state.currentNode = self.currentNode
        state.previousNode = self.previousNode
        state.nodeStackSize = self.nodeStack.size()
        state.indexStackSize = self.indexStack.size()
        # take snapshot of lookahead buffer
        state.lookahead = self.lookahead[:]

    def release(self, marker):
        raise NoSuchMethodError("can't release tree parse; email parrt@antlr.org")


    # Rewind the current state of the tree walk to the state it
    # was in when mark() was called and it returned marker.  Also,
    # wipe out the lookahead which will force reloading a few nodes
    # but it is better than making a copy of the lookahead buffer
    # upon mark().
    def rewind(self, marker=None):
        if not marker:
            marker = self.lastMarker

        if not self.markers or markers.size()< self.marker:
            # do nothing upon error; perhaps this should throw exception?
            return
        
        self.state = self.markers.get(marker-1)
        markers.remove(marker-1); # "pop" state from stack
        self.absoluteNodeIndex = state.absoluteNodeIndex
        self.currentChildIndex = state.currentChildIndex
        self.currentNode = state.currentNode
        self.previousNode = state.previousNode
        # drop node and index stacks back to old size
        self.nodeStack.setSize(state.nodeStackSize)
        self.indexStack.setSize(state.indexStackSize)
        self.lookahead = state.loolahead

    # consume() ahead until we hit index.  Can't just jump ahead--must
    # spit out the navigation nodes.
    def seek(self, index):
        if index < self.index():
            raise IllegalArgumentException("can't seek backwards in node stream")

        # seek forward, consume until we hit index
        while self.index() < index:
            consume()

    def index(self):
        return self.absoluteNodeIndex + 1

    # Expensive to compute so I won't bother doing the right thing.
    # This method only returns how much input has been seen so far.  So
    # after parsing it returns true size.
    def size(self):
        return absoluteNodeIndex+1

    # Satisfy Java's Iterator interface
    # TODO turn this into pythons generator interface
    def hasNext(self):
        return bool(self.currentNode)

    def next(self):
        # already walked entire tree; nothing to return
        if not self.currentNode:
            self.addLookahead(EOF_NODE)
            return None
        
        # initial condition (first time method is called)
        if self.currentChildIndex == -1:
            return handleRootNode()
        
        # index is in the child list?
        if self.currentChildIndex < currentNode.getChildCount():
            return visitChild(currentChildIndex)
        
        # hit end of child list, return to parent node or its parent ...
        walkBackToMostRecentNodeWithUnvisitedChildren()
        if self.currentNode:
            return visitChild(currentChildIndex)
            
        return None

    def handleRootNode(self):
        node = self.currentNode
        # poto first child in prep for subsequent next()
        self.currentChildIndex = 0
        if node.isNil():
            # don't count this root nil node
            node = visitChild(currentChildIndex)
            
        else:
            self.addLookahead(node)
            if self.currentNode.getChildCount()==0:
                # single node case
                self.currentNode = None # say we're done
                
                return node
            
    def visitChild(child):
        node = None
        # save state
        nodeStack.push(currentNode)
        indexStack.push(Integer(child))
        if child == 0 and not currentNode.isNil():
            addNavigationNode(Token.DOWN)

        # visit child
        self.currentNode = currentNode.getChild(child)
        self.currentChildIndex = 0
        node = currentNode;  # record node to return
        self.addLookahead(node)
        self.walkBackToMostRecentNodeWithUnvisitedChildren()
        return node

    # As we flatten the tree, we use UP, DOWN nodes to represent
    # the tree structure.  When debugging we need unique nodes
    # so instantiate ones when uniqueNavigationNodes is true.
    def addNavigationNode(self, ttype):
        node = None
        if ttype == DOWN:
            if self.hasUniqueNavigationNodes():
                node = NavDownNode()
            else:
                node = DOWN

        else:
            if self.hasUniqueNavigationNodes():
                node = NavUpNode()
            else:
                node = UP

        self.addLookahead(node)

    # Walk upwards looking for a node with more children to walk. #
    def walkBackToMostRecentNodeWithUnvisitedChildren(self):
        while self.currentNode and currentChildIndex >= currentNode.getChildCount():
            currentNode = nodeStack.pop()
            currentChildIndex = indexStack.pop()
            currentChildIndex += 1 # move to next child
            if currentChildIndex>=currentNode.getChildCount():
                if not currentNode.isNil():
                    addNavigationNode(Token.UP)
                    if currentNode == root: # we done yet?
                        currentNode = None


    def getTreeAdaptor(self):
        return self.adaptor

    def hasUniqueNavigationNodes(self):
        return self.uniqueNavigationNodes

    def setUniqueNavigationNodes(self, uniqueNavigationNodes):
        self.uniqueNavigationNodes = uniqueNavigationNodes

    def __str__(self):
        return str(root, None)

    def getLookaheadSize(self):
        return "FIXME"

    def getCurrentDetails(self, e):
        e.node = inputStream.LT(1)
        e.node.getCurrentDetails(e)

# A record of the rules used to match a token sequence.  The tokens
# end up as the leaves of this tree and rule nodes are the interior nodes.
# This really adds no functionality, it is just an alias for CommonTree
# that is more meaningful (specific) and holds a String to display for a node.
class ParseTree(BaseTree):

    def __init__(self, label):
        self.payload = label

    def dupNode(self):
        return None

    def getType(self):
        return 0

    def getText(self):
        return toString()

    def toString(self):
        return payload.toString()



# A parser for a stream of tree nodes.  "tree grammars" result in a subclass
# of self.  All the error reporting and recovery is shared with Parser via
# the BaseRecognizer superclass.
class TreeParser(BaseRecognizer):

    def __init__(self, input):
        self.setTreeNodeStream(input)

    # Set the input stream and reset the parser #
    def setTreeNodeStream(self, input):
        self.input = input
        reset()

    def getTreeNodeStream(self):
        return self.input

    def toTemplates(self, retvals):
        if not retvals:
            return None

        return [retval.getTemplate() for retval in retvals]

    def mismatch(self, input, ttype, follow):
        mte = MismatchedTreeNodeException(ttype, input)
        recoverFromMismatchedToken(input, mte, ttype, follow)


INVALID_NODE = CommonTree(INVALID_TOKEN)


#############################################################################
#
# streams for rule rewriting
#
#############################################################################

class RewriteRuleElementStream(object):
    """
    A generic list of elements tracked in an alternative to be used in
    a -> rewrite rule.  We need to subclass to fill in the next() method,
    which returns either an AST node wrapped around a token payload or
    an existing subtree.

    Once you start next()ing, do not try to add more elements.  It will
    break the cursor tracking I believe.

    @see org.antlr.runtime.tree.RewriteRuleSubtreeStream
    @see org.antlr.runtime.tree.RewriteRuleTokenStream
    
    TODO: add mechanism to detect/puke on modification after reading from
    stream
    """

    def __init__(self, adaptor, elementDescription, elements = None):
        # Cursor 0..n-1.  If singleElement!=null, cursor is 0 until you next(),
        # which bumps it to 1 meaning no more elements.
        self.cursor = 0

	# Track single elements w/o creating a list.  Upon 2nd add, alloc list
        self.singleElement = None

	# The list of tokens or subtrees we are tracking
        self.elements = None

	# The element or stream description; usually has name of the token or
	# rule reference that this list tracks.  Can include rulename too, but
	# the exception would track that info.
        self.elementDescription = elementDescription

        self.adaptor = adaptor

        if isinstance(elements, (list, tuple)):
            # Create a stream, but feed off an existing list
            self.singleElement = None
            self.elements = elements

        else:
            # Create a stream with one element
            self.add(elements)


    def reset(self):
        """
        Reset the condition of this stream so that it appears we have
        not consumed any of its elements.  Elements themselves are untouched.
        """
        
        self.cursor = 0

    def add(self, el):
        if el is None:
            return

        if self.elements is not None: # if in list, just add
            self.elements.append(el)
            return

        if self.singleElement is None: # no elements yet, track w/o list
            self.singleElement = el
            return

        # adding 2nd element, move to list
        self.elements = []
        self.elements.append(self.singleElement)
        self.singleElement = None
        self.elements.append(el)


    def next(self):
        """
        Return the next element in the stream.  If out of elements, throw
        an exception unless size()==1.  If size is 1, then return elements[0].
        
        Return a duplicate node/subtree if stream is out of elements and
        size==1.
	"""
        
        if self.cursor >= len(self) and len(self) == 1:
            # if out of elements and size is 1, dup
            el = self._next()
            return self.dup(el)

        # test size above then fetch
        el = self._next();
        return el;


    def _next(self):
        """
        do the work of getting the next element, making sure that it's
        a tree node or subtree.  Deal with the optimization of single-
        element list versus list of size > 1.  Throw an exception
        if the stream is empty or we're out of elements and size>1.
        protected so you can override in a subclass if necessary.
	"""

        if len(self) == 0:
            raise RewriteEmptyStreamException(self.elementDescription)
            
        if self.cursor >= len(self): # out of elements?
            if len(self) == 1: # if size is 1, it's ok; return and we'll dup 
                return self.singleElement

            # out of elements and size was not 1, so we can't dup
            raise RewriteCardinalityException(self.elementDescription)

        # we have elements
        if self.singleElement is not None:
            self.cursor += 1 # move cursor even for single element list
            return self.toTree(self.singleElement)

        # must have more than one in list, pull from elements
        o = self.toTree(self.elements[self.cursor])
        self.cursor += 1
        return o


    def dup(self, el):
        """
        When constructing trees, sometimes we need to dup a token or AST
        subtree.  Dup'ing a token means just creating another AST node
        around it.  For trees, you must call the adaptor.dupTree().
	"""

        raise NotImplementedError
    

    def toTree(self, el):
        """
        Ensure stream emits trees; tokens must be converted to AST nodes.
	AST nodes can be passed through unmolested.
        """

        return el


    def hasNext(self):
        return ( (self.singleElement is not None and self.cursor < 1)
                 or (self.elements is not None and self.cursor < len(self.elements))
                 )

                 
    def size(self):
        if self.singleElement is not None:
            return 1

        if self.elements is not None:
            return len(self.elements)

        return 0

    __len__ = size
    

    def getDescription(self):
        """Deprecated. Directly access elementDescription attribute"""
        
        return self.elementDescription


class RewriteRuleTokenStream(RewriteRuleElementStream):
    def toTree(self, el):
        return self.adaptor.createWithPayload(el)


    def dup(self, el):
        return self.adaptor.createWithPayload(el)


class RewriteRuleSubtreeStream(RewriteRuleElementStream):
    def nextNode(self):
        """
        Treat next element as a single node even if it's a subtree.
        This is used instead of next() when the result has to be a
        tree root node.  Also prevents us from duplicating recently-added
        children; e.g., ^(type ID)+ adds ID to type and then 2nd iteration
        must dup the type node, but ID has been added.

        Referencing a rule result twice is ok; dup entire tree as
        we can't be adding trees; e.g., expr expr. 
        """
        
        el = self._next()

        if self.cursor >= len(self) and len(self) == 1:
            # if out of elements and size is 1, dup just the node
            el = self.adaptor.dupNode(el)

        return el


    def dup(self, el):
        return self.adaptor.dupTree(el)
