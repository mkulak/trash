package main

import (
	"fmt";
	"net";
	"os";
)

func main() {
	var (
		host = "0.0.0.0";
		port = "843";
		remote = host + ":" + port;
		response = []byte("<cross-domain-policy><allow-access-from domain='*' to-ports='*' /></cross-domain-policy>\r\n");
	)
	fmt.Println("Initiating server... (Ctrl-C to stop)");

	lis, error := net.Listen("tcp", remote);
	defer lis.Close();
	if error != nil { 
		fmt.Printf("Error creating listener: %s\n", error ); 
		os.Exit(1); 
	}
	for {
		
		con, error := lis.Accept();
		if error != nil { fmt.Printf("Error: Accepting data: %s\n", error); os.Exit(2); }
		fmt.Printf("Handled request from: %s \n", con.RemoteAddr()); 
		//con.Write(strings.Bytes(response));
		con.Write(response);	
		con.Close();
	}
}
