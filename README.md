# Cache-performance-analysis
Programs that analyze performance of caches with different configurations in block size and set associativity



# Introduction
# ------------------------------
Cache performance can be improved in three ways.

Miss rate reduction.
Cache miss penalty reduction.
Cache hit time reduction.

Among those possible cache performance enhancement techniques, block size and cache associativity are two of the most common ones.
In this work, we wrote programs that analyze performance of caches with different configurations in block size and set associativity.


# General Assumptions
# ------------------------------
The given memory system is BYTE addressable.
The main memory size is 1024KB.
You need to generate 1024KB memory addresses.
The first address starts at 0x0.
Then, with a probability of 90% (use the random number generator to simulate this probability), the next address will be the previous address plus 1. Otherwise, the next address will be a randomly generated number between 500 to 1000. Then, the number gets added or subtracted from the current address by probability of 50% each. Note that the minimum address is 0 and the maximum is
1024K. When you subtract or add the random number from the current address, check whether it is less than 0 or greater than 1024K. Assume that your main memory is circular.
For example, 0 - 1 => 1024K, 1024K + 1 => 0, etc.
For example, 0x0, 0x1, 0x2, .... ,0xff, 0x2, ....
Assume that the given cache memory is an instruction cache. Thus, no write operations will be considered.
Assume that one clock cycle will be used when a cache hit happens.
Assume that log 2 (block_size_in_byte) clock cycles will be used to replace the block when a cache miss happens.
Use Random cache block replacement algorithm.  
When all blocks are filled and a new block must be loaded, a block is randomly chosen and replaced.


# Cache performance analysis program
# ------------------------------
We developed develop a simulator that analyzes performance of the following cache configurations.

1)  Cache performance analysis on different block sizes.
1. Cache size: 1K, 4K, 16K, 64K, 256K and 512K.
2. Block size: 16, 32, 64, 128 and 256 bytes.

2) Cache performance analysis on different set associativities. 
You can assume that block size is 32.
1. Cache size: 1,2,4,8,16,32,64,128 and 512K
2. Associativity: 1-way, 2-way, 4-way and 8-way

# Input
# ------------------------------
All possible combinations of the above design parameters
(e.g., cache size, block size and set associativity). 
Program is supposed to output performance measures of the given cache configurations.

# Output
# ------------------------------
Miss rate and average memory access time for the brief performance analysis report.

Note: For more details, check projectDetail.pdf

