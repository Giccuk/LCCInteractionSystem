%iterate 40/100->60/100
a(proposer(40,100,10,b),a)=>offer(40,100)=>a(responder(a),b).
a(responder(a),b)=>reject=>a(proposer(40,100,10,b),a).
a(proposer(50,100,10,b),a)=>offer(50,100)=>a(responder(a),b).
a(responder(a),b)=>reject=>a(proposer(50,100,10,b),a).
a(proposer(60,100,10,b),a)=>offer(60,100)=>a(responder(a),b).
a(responder(a),b)=>accept=>a(proposer(60,100,10,b),a).