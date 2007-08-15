/// \file
/// Provides the debugging functions invoked by a recognizer
/// built using the debug generator mode of the antlr tool.
/// See antlr3debugeventlistener.h for documentation.
///

#include    <antlr3.h>

static	ANTLR3_BOOLEAN	handshake		(pANTLR3_DEBUG_EVENT_LISTENER delboy);
static	void	enterRule				(pANTLR3_DEBUG_EVENT_LISTENER delboy, const char * ruleName);
static	void	enterAlt				(pANTLR3_DEBUG_EVENT_LISTENER delboy, int alt);
static	void	exitRule				(pANTLR3_DEBUG_EVENT_LISTENER delboy, const char * ruleName);
static	void	enterSubRule			(pANTLR3_DEBUG_EVENT_LISTENER delboy, int decisionNumber);
static	void	exitSubRule				(pANTLR3_DEBUG_EVENT_LISTENER delboy, int decisionNumber);
static	void	enterDecision			(pANTLR3_DEBUG_EVENT_LISTENER delboy, int decisionNumber);
static	void	exitDecision			(pANTLR3_DEBUG_EVENT_LISTENER delboy, int decisionNumber);
static	void	consumeToken			(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_COMMON_TOKEN t);
static	void	consumeHiddenToken		(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_COMMON_TOKEN t);
static	void	LT						(pANTLR3_DEBUG_EVENT_LISTENER delboy, int i, pANTLR3_COMMON_TOKEN t);
static	void	mark					(pANTLR3_DEBUG_EVENT_LISTENER delboy, ANTLR3_UINT64 marker);
static	void	rewindMark				(pANTLR3_DEBUG_EVENT_LISTENER delboy, int marker);
static	void	rewindLast				(pANTLR3_DEBUG_EVENT_LISTENER delboy);
static	void	beginBacktrack			(pANTLR3_DEBUG_EVENT_LISTENER delboy, int level);
static	void	endBacktrack			(pANTLR3_DEBUG_EVENT_LISTENER delboy, int level, ANTLR3_BOOLEAN successful);
static	void	location				(pANTLR3_DEBUG_EVENT_LISTENER delboy, int line, int pos);
static	void	recognitionException	(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_EXCEPTION e);
static	void	beginResync				(pANTLR3_DEBUG_EVENT_LISTENER delboy);
static	void	endResync				(pANTLR3_DEBUG_EVENT_LISTENER delboy);
static	void	semanticPredicate		(pANTLR3_DEBUG_EVENT_LISTENER delboy, ANTLR3_BOOLEAN result, const char * predicate);
static	void	commence				(pANTLR3_DEBUG_EVENT_LISTENER delboy);
static	void	terminate				(pANTLR3_DEBUG_EVENT_LISTENER delboy);
static	void	consumeNode				(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_BASE_TREE t);
static	void	LTT						(pANTLR3_DEBUG_EVENT_LISTENER delboy, int i, pANTLR3_BASE_TREE t);
static	void	nilNode					(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_BASE_TREE t);
static	void	createNode				(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_BASE_TREE t);
static	void	createNodeTok			(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_BASE_TREE node, pANTLR3_COMMON_TOKEN token);
static	void	becomeRoot				(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_BASE_TREE newRoot, pANTLR3_BASE_TREE oldRoot);
static	void	addChild				(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_BASE_TREE root, pANTLR3_BASE_TREE child);
static	void	setTokenBoundaries		(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_BASE_TREE t, int tokenStartIndex, int tokenStopIndex);
static	void	freeDel					(pANTLR3_DEBUG_EVENT_LISTENER delboy);

/// Create and initialize a new debug event listener that can be connected to
/// by ANTLRWorks and any other debugger via a socket.
///
ANTLR3_API pANTLR3_DEBUG_EVENT_LISTENER
antlr3DebugListenerNew()
{
	pANTLR3_DEBUG_EVENT_LISTENER	delboy;

	delboy = ANTLR3_MALLOC(sizeof(ANTLR3_DEBUG_EVENT_LISTENER));

	if	(delboy == NULL)
	{
		return (pANTLR3_DEBUG_EVENT_LISTENER) ANTLR3_FUNC_PTR(ANTLR3_ERR_NOMEM);
	}

	// Initialize the API
	//
	delboy->addChild				= addChild;
	delboy->becomeRoot				= becomeRoot;
	delboy->beginBacktrack			= beginBacktrack;
	delboy->beginResync				= beginResync;
	delboy->commence				= commence;
	delboy->consumeHiddenToken		= consumeHiddenToken;
	delboy->consumeNode				= consumeNode;
	delboy->consumeToken			= consumeToken;
	delboy->createNode				= createNode;
	delboy->createNodeTok			= createNodeTok;
	delboy->endBacktrack			= endBacktrack;
	delboy->endResync				= endResync;
	delboy->enterAlt				= enterAlt;
	delboy->enterDecision			= enterDecision;
	delboy->enterRule				= enterRule;
	delboy->enterSubRule			= enterSubRule;
	delboy->exitDecision			= exitDecision;
	delboy->exitRule				= exitRule;
	delboy->exitSubRule				= exitSubRule;
	delboy->handshake				= handshake;
	delboy->location				= location;
	delboy->LT						= LT;
	delboy->LTT						= LTT;
	delboy->mark					= mark;
	delboy->nilNode					= nilNode;
	delboy->recognitionException	= recognitionException;
	delboy->rewind					= rewindMark;
	delboy->rewindLast				= rewindLast;
	delboy->semanticPredicate		= semanticPredicate;
	delboy->setTokenBoundaries		= setTokenBoundaries;
	delboy->terminate				= terminate;

	delboy->PROTOCOL_VERSION		= 1;

	return delboy;
}

pANTLR3_DEBUG_EVENT_LISTENER
antlr3DebugListenerNewPort(ANTLR3_UINT32 port)
{
	pANTLR3_DEBUG_EVENT_LISTENER	delboy;

	delboy		 = antlr3DebugListenerNew();

	if	(delboy != ANTLR3_FUNC_PTR(ANTLR3_ERR_NOMEM))
	{
		delboy->port = port;
	}

	return delboy;
}

static int sockSend(SOCKET sock, const char * ptr, size_t len)
{
	size_t		sent;
	int		thisSend;

	sent	= 0;
		
	while	(sent < len)
	{
		// Send as many bytes as we can
		//
		thisSend =	send(sock, ptr, len - sent, 0);

		// Check for errors and tell the user if we got one
		//
		if	(thisSend	== -1)
		{
			return	ANTLR3_FALSE;
		}

		// Increment our offset by how many we were able to send
		//
		ptr			+= thisSend;
		sent		+= thisSend;
	}
	// Everything is OK
	//
	return	ANTLR3_TRUE;
}

static	ANTLR3_BOOLEAN	
handshake				(pANTLR3_DEBUG_EVENT_LISTENER delboy)
{
	/// Connection structure with which to wait and accept a connection from
	/// a debugger.
	///
	SOCKET				serverSocket;

	// Connection structures to deal with the client after we accept the connection
	// and the server while we accept a connection.
	//
	struct sockaddr_in	client;
	struct sockaddr_in	server;

	// Buffer to construct our message in
	//
	char	message[256];

	// Specifies the length of the connection structure to accept()
	// Windows use int, everyone else uses size_t
	//
	ANTLR3_SALENT				sockaddr_len;



	// Option holder for setsockopt()
	//
	int		optVal;

	if	(delboy->initialized == ANTLR3_FALSE)
	{
		// Windows requires us to initialize WinSock.
		//
#ifdef _WIN32
		{
			WORD		wVersionRequested;
			WSADATA		wsaData;
			int			err;			// Return code from WSAStartup

			// We must initialise the Windows socket system when the DLL is loaded.
			// We are asking for Winsock 1.1 or better as we don't need anything
			// too complicated for this.
			//
			wVersionRequested = MAKEWORD( 1, 1);

			err = WSAStartup( wVersionRequested, &wsaData );

			if ( err != 0 ) 
			{
				// Tell the user that we could not find a usable
				// WinSock DLL
				//
				return FALSE;
			}
		}
#endif

		// Create the server socket, we are the server because we just wait until
		// a debugger connects to the port we are listening on.
		//
		serverSocket	= socket(AF_INET, SOCK_STREAM, 0);

		if	(serverSocket == INVALID_SOCKET)
		{
			return ANTLR3_FALSE;
		}

		// Set the listening port
		//
		server.sin_port			= htons((unsigned short)delboy->port);
		server.sin_family		= AF_INET;
		server.sin_addr.s_addr	= htonl (INADDR_ANY);

		// We could allow a rebind on the same addr/port pair I suppose, but
		// I imagine that most people will just want to start debugging one parser at once.
		// Maybe change this at some point, but rejecting the bind at this point will ensure
		// that people realize they have left something running in the background.
		//
		if	(bind(serverSocket, (const struct sockaddr *)&server, sizeof(server)) == -1)
		{
			return ANTLR3_FALSE;
		}

		// We have bound the socket to the port and address so we now ask the TCP subsystem
		// to start listening on that address/port
		//
		if	(listen(serverSocket, 1) == -1)
		{
			// Some error, just fail
			//
			return	ANTLR3_FALSE;
		}

		// Now we can try to accept a connection on the port
		//
		sockaddr_len	= sizeof(client);
		delboy->socket	= accept(serverSocket, (pANTLR3_SOCKADDR)&client, &sockaddr_len);

		// Having accepted a connection, we can stop listening and close down the socket
		//
		shutdown		(serverSocket, 0x02);
		closesocket		(serverSocket);

		if	(delboy->socket == -1)
		{
			return ANTLR3_FALSE;
		}

		// Disable Nagle as this is essentially a chat exchange
		//
		optVal	= 1;
		setsockopt(delboy->socket, SOL_SOCKET, TCP_NODELAY, (const void *)&optVal, sizeof(optVal));
		
	}

	// We now have a good socket connection with the debugging client, so we
	// send it the protocol version we are using
	//
	sprintf(message, "ANTLR %d", delboy->PROTOCOL_VERSION);
	sockSend(delboy->socket, message, strlen(message));

	delboy->initialized = ANTLR3_TRUE;

	return	ANTLR3_TRUE;
}

static	void	
enterRule				(pANTLR3_DEBUG_EVENT_LISTENER delboy, const char * ruleName)
{
}

static	void	
enterAlt				(pANTLR3_DEBUG_EVENT_LISTENER delboy, int alt)
{
}

static	void	
exitRule				(pANTLR3_DEBUG_EVENT_LISTENER delboy, const char * ruleName)
{
}

static	void	
enterSubRule			(pANTLR3_DEBUG_EVENT_LISTENER delboy, int decisionNumber)
{
}

static	void	
exitSubRule				(pANTLR3_DEBUG_EVENT_LISTENER delboy, int decisionNumber)
{
}

static	void	
enterDecision			(pANTLR3_DEBUG_EVENT_LISTENER delboy, int decisionNumber)
{
}

static	void	
exitDecision			(pANTLR3_DEBUG_EVENT_LISTENER delboy, int decisionNumber)
{
}

static	void	
consumeToken			(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_COMMON_TOKEN t)
{
}

static	void	
consumeHiddenToken		(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_COMMON_TOKEN t)
{
}

static	void	
LT						(pANTLR3_DEBUG_EVENT_LISTENER delboy, int i, pANTLR3_COMMON_TOKEN t)
{
}

static	void	
mark					(pANTLR3_DEBUG_EVENT_LISTENER delboy, ANTLR3_UINT64 marker)
{
}

static	void	
rewindMark					(pANTLR3_DEBUG_EVENT_LISTENER delboy, ANTLR3_UINT64 marker)
{
}

static	void	
rewindLast				(pANTLR3_DEBUG_EVENT_LISTENER delboy)
{
}

static	void	
beginBacktrack			(pANTLR3_DEBUG_EVENT_LISTENER delboy, int level)
{
}

static	void	
endBacktrack			(pANTLR3_DEBUG_EVENT_LISTENER delboy, int level, ANTLR3_BOOLEAN successful)
{
}

static	void	
location				(pANTLR3_DEBUG_EVENT_LISTENER delboy, int line, int pos)
{
}

static	void	
recognitionException	(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_EXCEPTION e)
{
}

static	void	
beginResync				(pANTLR3_DEBUG_EVENT_LISTENER delboy)
{
}

static	void	
endResync				(pANTLR3_DEBUG_EVENT_LISTENER delboy)
{
}

static	void	
semanticPredicate		(pANTLR3_DEBUG_EVENT_LISTENER delboy, ANTLR3_BOOLEAN result, const char * predicate)
{
}

static	void	
commence				(pANTLR3_DEBUG_EVENT_LISTENER delboy)
{
}

static	void	
terminate				(pANTLR3_DEBUG_EVENT_LISTENER delboy)
{
}

static	void	
consumeNode				(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_BASE_TREE t)
{
}

static	void	
LTT						(pANTLR3_DEBUG_EVENT_LISTENER delboy, int i, pANTLR3_BASE_TREE t)
{
}

static	void	
nilNode					(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_BASE_TREE t)
{
}

static	void	
createNode				(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_BASE_TREE t)
{
}

static	void	
createNodeTok			(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_BASE_TREE node, pANTLR3_COMMON_TOKEN token)
{
}

static	void	
becomeRoot				(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_BASE_TREE newRoot, pANTLR3_BASE_TREE oldRoot)
{
}

static	void	
addChild				(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_BASE_TREE root, pANTLR3_BASE_TREE child)
{
}

static	void	
setTokenBoundaries		(pANTLR3_DEBUG_EVENT_LISTENER delboy, pANTLR3_BASE_TREE t, int tokenStartIndex, int tokenStopIndex)
{
}


