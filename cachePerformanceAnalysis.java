import java.io.BufferedReader;
import java.io.DataInputStream;
import java.util.Random; 
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.text.DecimalFormat;

public class cachePerformanceAnalysis {

	static int memorySize;
	static int blockSize;
	static int associativity;
	static int numOfBlocksInMainMemory;
	static int numOfBlocksInCache;
	static int numOfFramesInCache;
	static int maxNumOfInstToBeExecuted;
	static int instCount = 0;
	static int instBlockToBeSearchedInCache;
	static int instToBeSearchedInCache;
	static int numOfInstructionInOneCacheFrame;

	static int numOfMiss=0;
	static int[][] mainMemory;
	static int[][] PMT;
	static int memoryAddress=0;
	static int firstEmptyFrame = 0;
	static ArrayList<Integer> cacheSizesForAnalysisOne;
	static ArrayList<Integer> blockSizesForAnalysisOne;
	static ArrayList<Integer> cacheSizesForAnalysisTwo;
	static ArrayList<Integer> associativitiesForAnalysisTwo;

	public static void main(String[] args) throws IOException
	{
		
			//get all the elements from input file into array list
			ArrayList<ArrayList<Integer>> arrayOfInputFileLines = arrayOfInputFileLines("cacheInput.txt");
		
			//initialize all configuration variables with input file values
			cacheSizesForAnalysisOne = new ArrayList<Integer>();
    		blockSizesForAnalysisOne = new ArrayList<Integer>();
    		cacheSizesForAnalysisTwo = new ArrayList<Integer>();
    		associativitiesForAnalysisTwo = new ArrayList<Integer>();

       		if(arrayOfInputFileLines.size()>0)
       			maxNumOfInstToBeExecuted = arrayOfInputFileLines.get(0).get(0);      
    		if(arrayOfInputFileLines.size()>1)
    			cacheSizesForAnalysisOne.addAll(arrayOfInputFileLines.get(1));
        	if(arrayOfInputFileLines.size()>2)
           		blockSizesForAnalysisOne.addAll(arrayOfInputFileLines.get(2));
        	if(arrayOfInputFileLines.size()>3)
           		cacheSizesForAnalysisTwo.addAll(arrayOfInputFileLines.get(3));
       		if(arrayOfInputFileLines.size()>4)
           		associativitiesForAnalysisTwo.addAll(arrayOfInputFileLines.get(4));        	
  	
        	
    		
    		System.out.println("\n\n---------------CONFIGURATION FOR CACHE ANALYSIS---------------\n");
    		System.out.println("Cache_Sizes for performance analysis on different block size: "+cacheSizesForAnalysisOne);
    		System.out.println("Block_Sizes for performance analysis on different block size: "+blockSizesForAnalysisOne);
    		System.out.println("Cache_Sizes for performance analysis on different set associativities: "+cacheSizesForAnalysisTwo);
    		System.out.println("Associativities for performance analysis on different set associativities: "+associativitiesForAnalysisTwo);

    		memorySize = 1048576;

    		if(cacheSizesForAnalysisOne.size()>0 && blockSizesForAnalysisOne.size()>0)
    			performanceAnalysisOnDifferentBlockSizes();
    		
    		if(cacheSizesForAnalysisTwo.size()>0 && associativitiesForAnalysisTwo.size()>0)
    			performanceAnalysisOnDifferentSetAssociativities(); 
    		
	}//EOD main
	
	
	/**
	 * The method generates a random, provided low and high range of values.
	 * @param min : low range for random number 
	 * @param max : high range for random number 
	 * @return int number as random number.
	 */
	public static int randomInteger(int min, int max) 
	{		
		 Random rand = new Random(); 		 
		 int randomNum = rand.nextInt(max-min+1) + min; 
		 return randomNum;
		 
	}//EOD randomInteger
	
	
	/**
	 * The method reads a input file into a array of array.
	 * @param fileName : name of input file 
	 * @return ArrayList<ArrayList<Integer>>: Array of all the possible configurations read from input file.
	 * @throws FileNotFoundException and IOException when file is not found or when some i/o exception occurs.
	 */
	private static ArrayList<ArrayList<Integer>> arrayOfInputFileLines(String fileName) throws FileNotFoundException,IOException 
	{
		FileInputStream fileInputStream = new FileInputStream(fileName);
		DataInputStream dataInputStream = new DataInputStream(fileInputStream);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
		String strLine;
		int i=0;
		
		ArrayList<ArrayList<Integer>> arrayOfInputFileLines = new ArrayList<ArrayList<Integer>>();
		
		while ((strLine = bufferedReader.readLine()) != null && strLine.length()!=0)
		{              
            	StringTokenizer st = new StringTokenizer(strLine, ",");  
           		ArrayList<Integer> intArr = new ArrayList<Integer>();
            	while (st.hasMoreElements()) 
            	{
                		String element = ((String)st.nextElement());
                		intArr.add(Integer.parseInt(element));
          		}
         		arrayOfInputFileLines.add(i, intArr); 
            	i++;
		}
		
		dataInputStream.close();
		return arrayOfInputFileLines;
		
	}//EOD arrayOfInputFileLines
	
	
	/**
	 * The method searches cache for the given instruction, if the instruction 
	 * is not found in cache, updates the cache frame with missing instruction block.
	 */
	public static void searchCacheForInstruction()
	{
		boolean isHit = false;
		
		for(int p=0; p<numOfFramesInCache; p++) 
    	{
			for(int q=0; q<numOfInstructionInOneCacheFrame; q++) 
			{
				if(PMT[p][q]==instToBeSearchedInCache) { isHit = true; break; }
			}
			if(isHit)break;
    	}	

		if(isHit==false) 
		{
			numOfMiss++;
			int blockNumer = instToBeSearchedInCache/blockSize;
			updateCacheWithBlocksStartingAtAddress(blockNumer);
		}
				
		memoryAddress = getNextInstruction();
		instCount++;
		int nextInstBlockNumber = memoryAddress/blockSize;
		int nextInstIndexWithinBlock = memoryAddress%blockSize;
		instToBeSearchedInCache = mainMemory[nextInstBlockNumber][nextInstIndexWithinBlock];//instBlock;	

	}//EOD searchCacheForNextBlockAddress

	
	/**
	 * The method generates next instruction with a probability of 90%, the next 
	 * address will be the previous instruction plus 1. Otherwise, the next 
	 * address will be a randomly generated number between 500 to 1000. Then, the 
	 * number gets added or subtracted from the current address by probability of 50% each.
	 */
	public static int getNextInstruction() 
	{
		int currentAddress = memoryAddress;
		int nextAddress = 0;
		int min =0;
		int max = memorySize-1;
		
		if(max<min)
			max=min;
		
		int randomNumber = randomInteger(1,10);
		int nintyPercentProbablity = 9;
		if(randomNumber<=nintyPercentProbablity)
		{
			nextAddress = currentAddress+1;
	    		if(nextAddress>max)
				nextAddress = min;
		}
		else
		{
			int displacement = randomInteger(500,1000);	    	
			randomNumber = randomInteger(1,10);
			int fiftyPercentProbablity = 5;
			
			if(randomNumber<fiftyPercentProbablity)
	    			nextAddress = currentAddress - displacement;
	    		else
	    			nextAddress = currentAddress + displacement;    	
	    	
	    		if(nextAddress>max)
	    		{
	    			int differenceBetweenMaxAndMin = nextAddress-max;
	    			nextAddress = min + differenceBetweenMaxAndMin;
	    		}
	    		else if(nextAddress<min)
	    		{
	    			nextAddress = max + nextAddress;
	    		}  
		}
    	
		if(nextAddress <=0 && nextAddress>=(mainMemory.length-1))
		{
			nextAddress = min;
		}
		
    	return nextAddress;
		
	}//EOD getNextInstruction
	
	
	/**
	 * The method checks if the cache is empty.
	 * If there is any empty frame, it sets firstEmptyFrame static variable 
	 * to the empty frame address
	 */
	public static boolean checkIfCachehasEmptyFrame()
	{
		boolean hasEmptyFrame = false;
		
		for(int p=0; p<numOfFramesInCache; p++) 
	    {
			if(PMT[p][0] == -1)
			{
				hasEmptyFrame = true;
				firstEmptyFrame=p;
				break;
			}
	    }	
		return hasEmptyFrame;
		
	}//EOD checkIfCachehasEmptyFrame

	
	/**
	 * The method updates the cache frame with the blocks from memory addresses.
	 * If there is any empty frame, it sets firstEmptyFrame static variable 
	 * to the empty frame address
	 * @param startingAddress : address of the block from main memory
	 */
	public static void updateCacheWithBlocksStartingAtAddress(int blockNumber) 
	{	
		int frameToBeDisplaced = 0;
		int min = 0;
		int max = numOfFramesInCache-1;
		if(max<min)
			max = min;
		frameToBeDisplaced = randomInteger(min,max);
				
		int i = 0; 
		while(i<numOfInstructionInOneCacheFrame)
		{
			for(int k=0;k<blockSize;k++)
			{
				if(blockNumber<numOfBlocksInMainMemory-1)
				{
					PMT[frameToBeDisplaced][i]=mainMemory[blockNumber][k];	
				}
				i++;
			}
			blockNumber++;
		}	
	}//EOD updateCacheWithBlocksStartingAtAddress
	
	
	
	/**
	 * The method checks cache-performance-analysis on different block sizes.
	 * @param  
	 */
	public static void performanceAnalysisOnDifferentBlockSizes()
	{
		 System.out.println("\n---------------PERFORMANCE ANALYSIS ON DIFFERENT BLOCK SIZES---------------\n");
		 associativity = 1;
		 System.out.println("Associativity = "+associativity+"\n");

		 for(int i=0;i<cacheSizesForAnalysisOne.size();i++)
		 {
			 int cacheSize = cacheSizesForAnalysisOne.get(i);
			 
			 System.out.println("\nCache Size = "+cacheSize);
			 System.out.println("-------------------------");
			 
			 for(int j=0;j<blockSizesForAnalysisOne.size();j++)
			 {
				 	memoryAddress=numOfMiss=instCount=numOfBlocksInMainMemory=numOfBlocksInCache= numOfFramesInCache=instBlockToBeSearchedInCache=0;
				 	blockSize = blockSizesForAnalysisOne.get(j);	
		    	 	numOfBlocksInMainMemory = memorySize/blockSize;
		    	 	numOfInstructionInOneCacheFrame = blockSize*associativity;
	    			numOfFramesInCache = cacheSize/numOfInstructionInOneCacheFrame;
	    		 
		    		//create main memory
			    	int InstructionNumber = 0;
		    		mainMemory = new int[numOfBlocksInMainMemory][blockSize];
		    		
		    		for (int p = 0; p < numOfBlocksInMainMemory; p++) 
		    		{ 
			    		for (int q = 0; q < blockSize; q++) 
			    		{
			    			mainMemory[p][q] = InstructionNumber;
			    			InstructionNumber++;
			    		}
		    		}
		    		
		    		//create cache memory
		    		PMT = new int[numOfFramesInCache][numOfInstructionInOneCacheFrame];
		    		for(int p=0; p<numOfFramesInCache; p++) 
			    	{
		    			for(int q=0; q<numOfInstructionInOneCacheFrame; q++) 
		    			{
		    				PMT[p][q] = -1;
		    			}
			    	}			    	 	
				
		    		instToBeSearchedInCache =  mainMemory[0][0];
		    		memoryAddress = instToBeSearchedInCache;
		    		instCount++;
				 
		    		for(int p=0;p<maxNumOfInstToBeExecuted;p++)
		    		{
		    			searchCacheForInstruction();
		    		}

		    		DecimalFormat df = new DecimalFormat();
		    		df.setMaximumFractionDigits(6);
				 
		    		double missRate = (double)(numOfMiss)/(maxNumOfInstToBeExecuted);
					double missPercentage = missRate*100;
					double hitRate = 1-missRate;
					double hitTime = 1;
					double missPenalty = ((int)(Math.log(blockSize)/ Math.log(2))); 
					double avgMemoryAccessTime = (hitRate*hitTime)+(missRate*missPenalty);
		    		System.out.println("\tCache_Size = "+blockSize+"   Miss_Rate = "+df.format(missPercentage)+"   AMAT = "+df.format(avgMemoryAccessTime));
			 }		 
			 System.out.print("\n");
	 	 }
	}//EOD performanceAnalysisOnDifferentBlockSizes
	
	

	/**
	 * The method checks cache-performance-analysis on different set associativities.
	 */
	public static void performanceAnalysisOnDifferentSetAssociativities()
	{
		System.out.println("\n---------------PERFORMANCE ANALYSIS ON DIFFERENT SET ASSOCIATIVITIES---------------\n");
		blockSize = 32;
		System.out.println("Block_Size = "+blockSize+"\n"); 
		
		for(int i=0;i<associativitiesForAnalysisTwo.size();i++)
		{
			associativity = associativitiesForAnalysisTwo.get(i);			    	

			System.out.println("\nAssociativity = "+associativity);
			System.out.println("-------------------------");

			for(int j=0;j<cacheSizesForAnalysisTwo.size();j++)
			{
			 	memoryAddress=numOfMiss=instCount=numOfBlocksInMainMemory=numOfBlocksInCache= numOfFramesInCache=instBlockToBeSearchedInCache=0;
				int cacheSize = cacheSizesForAnalysisTwo.get(j);
				numOfBlocksInMainMemory = memorySize/blockSize;
	    	 	numOfInstructionInOneCacheFrame = blockSize*associativity;
    			numOfFramesInCache = cacheSize/(blockSize*associativity);

				//create main memory
		    	int InstructionNumber = 0;
	    		mainMemory = new int[numOfBlocksInMainMemory][blockSize];
	    		
	    		for (int p = 0; p < numOfBlocksInMainMemory; p++) 
	    		{ 
		    		for (int q = 0; q < blockSize; q++) 
		    		{
		    			mainMemory[p][q] = InstructionNumber;
		    			InstructionNumber++;
		    		}
	    		}
	    		
	    		//create cache memory
	    		PMT = new int[numOfFramesInCache][numOfInstructionInOneCacheFrame];
	    		for(int p=0; p<numOfFramesInCache; p++) 
		    	{
	    			for(int q=0; q<numOfInstructionInOneCacheFrame; q++) 
	    			{
	    				PMT[p][q] = -1;
	    			}
		    	}			    	 	
			
	    		instToBeSearchedInCache =  mainMemory[0][0];
	    		memoryAddress = instToBeSearchedInCache;
	    		instCount++;
			 
	    		for(int p=0;p<maxNumOfInstToBeExecuted;p++)
	    		{
	    			searchCacheForInstruction();
	    		}

				DecimalFormat df = new DecimalFormat();
				df.setMaximumFractionDigits(6);
				
	    		double missRate = (double)(numOfMiss)/(maxNumOfInstToBeExecuted);
				double missPercentage = missRate*100;
				double hitRate = 1-missRate;
				double hitTime = 1;
				double missPenalty = ((int)(Math.log(blockSize)/ Math.log(2))); 
				double avgMemoryAccessTime = (hitRate*hitTime)+(missRate*missPenalty);
	    		System.out.println("\tCache_Size = "+cacheSize+"   Miss_Rate = "+df.format(missPercentage)+"   AMAT = "+df.format(avgMemoryAccessTime));

			 }
			 System.out.print("\n");
		 }
	}

}// EOD pgm


	
