service BloomFilter {

 // counting bloom filter

 oneway void inc(1:i64 id,2:i64 content),
 i32 count(1:i64 id,2:i64 content),
 oneway void dec(1:i64 id,2:i64 content),

 // membership bloom filter

 oneway void add(1:i64 id,2:i64 content),
 bool contains(1:i64 id,2:i64 content),
 map<i64,list<i64> > bulk_contains(1:list<i64> ids,2:list<i64> contents),
 // aged bloom filter

 oneway void aged_inc(1:i64 id,2:i64 content),
 i32 aged_count(1:i64 id,2:i64 content),
 oneway void aged_dec(1:i64 id,2:i64 content),

 // threshold bloom filter

 oneway void commonality_add(1:i64 id,2:i64 content),
 oneway void direct_commonality_add(1:i64 id,2:i64 content),
 bool is_common(1:i64 id,2:i64 content),		     

 void delete_bloom_filter(1:i64 id),
 bool is_bloom_filter_present(1:i64 id)
}