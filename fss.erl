-module(fss).
-author('kleshney@gmail.com').
-export([start/0, listen/1]).
-define(PORTNO, 843).
-define(RESPONSE, "<cross-domain-policy><allow-access-from domain='*' to-ports='*' /></cross-domain-policy>").

start() -> spawn_link(?MODULE, listen, [?PORTNO]).

listen(Port) ->
    case gen_tcp:listen(Port, [binary, {packet, 0}, {active, false}]) of
        {ok, ServerSocket} -> 
            loop(ServerSocket);
        {error, Reason} -> 
            throw({'cant bind socket', Reason})
    end.

loop(ServerSocket) ->
    case gen_tcp:accept(ServerSocket) of
        {ok, Socket} ->
            gen_tcp:send(Socket, [?RESPONSE] ++ [13, 10]),
            gen_tcp:close(Socket),
            loop(ServerSocket);
        _ -> 
            loop(ServerSocket)
    end.
