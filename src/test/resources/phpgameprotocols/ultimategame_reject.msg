%reject 10/100
a(proposer(10,100,r),p)=>offer(10,100)=>a(responder(p),r).
a(responder(p),r)=>reject=>a(proposer(10,100,r),p).

