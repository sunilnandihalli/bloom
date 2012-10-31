namespace java bloomier

typedef i32 UserId
typedef i32 ContentsHash
typedef i32 CountType

struct Query {
  1: UserId uid,
  2: ContentsHash content
}

struct Result {
  1: UserId uid,
  2: ContentsHash content,
  3: CountType count
}

service UserHistory {
	//	      Result count(1:Query q),
  Result count(1:UserId uid,2:ContentsHash content),
//	   oneway void visit(1:Query q)
  oneway void visit(1:UserId uid,2:ContentsHash content)
}
