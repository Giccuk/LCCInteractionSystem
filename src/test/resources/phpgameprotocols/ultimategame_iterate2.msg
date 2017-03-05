%iterate 20/100->50/100
a(proposer(20,100,10,b),a)=>offer(20,100)=>a(responder(a),b).
a(responder(a),b)=>reject=>a(proposer(20,100,10,b),a).
a(proposer(30,100,10,b),a)=>offer(30,100)=>a(responder(a),b).
a(responder(a),b)=>reject=>a(proposer(30,100,10,b),a).
a(proposer(40,100,10,b),a)=>offer(40,100)=>a(responder(a),b).
a(responder(a),b)=>reject=>a(proposer(40,100,10,b),a).
a(proposer(50,100,10,b),a)=>offer(50,100)=>a(responder(a),b).
a(responder(a),b)=>accept=>a(proposer(50,100,10,b),a).