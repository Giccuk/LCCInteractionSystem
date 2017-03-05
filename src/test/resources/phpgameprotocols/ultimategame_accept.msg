%accept 50/100
a(proposer(50,100,b),a)=>offer(50,100)=>a(responder(a),b).
a(responder(a),b)=>accept=>a(proposer(50,100,b),a).


