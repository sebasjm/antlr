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

from copy import deepcopy

from antlr3.constants import INVALID_TOKEN_TYPE, UP, DOWN, EOF
from antlr3.recognizers import BaseRecognizer
#from antlr3.streams import IntStream
from antlr3.tokens import CommonToken, Token

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
        pass

    def getChildCount(self):
        pass

    def addChild(self, t):
        """Add t as a child to this node.  If t is null, do nothing.  If t
        is nil, add all children of t to this' children.
        @param t
        """
        pass

    def isNil(self):
        """Indicates the node is a nil node but may still have children, meaning
        the tree is a flat list.
        """
        pass

    def dupTree(self):
        pass

    def dupNode(self):
        pass

    def getType(self):
        """Return a token type; needed for tree parsing."""
        pass

    def getText(self):
        pass

    def getLine(self):
        """In case we don't have a token payload, what is the line for errors?"""
        pass

    def getCharPositionInLine(self):
        pass


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

class TreeAdaptor(object):
    def create(self, payload, text=None):
        pass

    def dupTree(self, tree):
        pass

    def dupNode(self, treeNode):
        pass 

    def nil(self):
        pass

    def rulePostProcessing(self, root):
        pass 

    def getUniqueID(self, node):
        pass 

    def addChild(self, t, child):
        pass

    def becomeRoot(self, newRoot, oldRoot):
        pass

    def getType(self, t):
        pass

    def setType(self, t, tokenType):
        pass

    def getText(self, t):
        pass

    def setText(self, t, text):
        pass

    def setTokenBoundaries(self, t, startToken, stopToken):
        pass

    def getTokenStartIndex(self, t):
        pass

    def getTokenStopIndex(self, t):
        pass

    def getChild(self, t, i):
        pass

    def getChildCount(self, t):
        pass


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
            newRoot = self.create(newRoot)

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
        if r.isNil() and r.getChildCount() == 1:
            r = r.getChild(0)

        return r

    def addChild(self, tree, child):
        if isinstance(child, Token):
            child = self.create(child)

        return tree.addChild(child)

    def create(self, payload, text=None):
        node = CommonTree(payload)

        if text is not None:
            node.text = text

        return node

    def nil(self):
        return self.create(None)


import traceback

# A tree node that is wrapper for a Token object. #
class CommonTree(BaseTree):
    # What token indexes bracket all tokens associated with this node
    # and below?
    def __init__(self, token=None):
        BaseTree.__init__(self)
        self.token = token
        self.startIndex = -1
        self.stopIndex = -1
        
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
        if fromToken is None and tokenType is not None and text is not None:
            fromToken = self.create(tokenType, text)

        elif tokenType is not None or text is not None:
            fromToken = self.create(fromToken, fromToken.text)
            
            if tokenType is not None:
                fromToken.type = tokenType
            if text is not None:
                fromToken.text = text

        return self.create(fromToken)


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


# A stream of tree nodes, accessing nodes from a tree of some kind #
class TreeNodeStream(object): #IntStream):
    def LT(self, k):
        pass

    def getTreeSource(self):
        pass

    def getTreeAdaptor(self):
        pass

    def setUniqueNavigationNodes(self, uniqueNavigationNodes):
        pass

    def toString(self, start, stop):
        pass


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


INVALID_NODE = CommonTree(INVALID_TOKEN_TYPE)


