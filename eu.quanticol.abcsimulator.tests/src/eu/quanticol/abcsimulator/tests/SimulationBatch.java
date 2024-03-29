/**
 * 
 */
package eu.quanticol.abcsimulator.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;

import org.cmg.ml.sam.sim.SimulationEnvironment;
import org.cmg.ml.sam.sim.sampling.SamplingCollection;
import org.cmg.ml.sam.sim.sampling.SimulationTimeSeries;
import org.cmg.ml.sam.sim.sampling.StatisticSampling;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.ErrorBarType;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import eu.quanticol.abcsimulator.AbCSystem;
import eu.quanticol.abcsimulator.AverageDeliveryTime;
import eu.quanticol.abcsimulator.AverageInputQueueSize;
import eu.quanticol.abcsimulator.AverageMessageInterval;
import eu.quanticol.abcsimulator.AverageWaitingQueueSize;
import eu.quanticol.abcsimulator.MaxDeliveryTime;
import eu.quanticol.abcsimulator.MaxMessageInterval;
import eu.quanticol.abcsimulator.MinDeliveryTime;
import eu.quanticol.abcsimulator.MinMessageInterval;
import eu.quanticol.abcsimulator.NumberOfDeliveredMessages;
import eu.quanticol.abcsimulator.P2PStructureFactory;
import eu.quanticol.abcsimulator.RingStructureFactory;
import eu.quanticol.abcsimulator.SingleServerFactory;
import eu.quanticol.abcsimulator.TravellingMessages;
import eu.quanticol.abcsimulator.TreeStructureFactory;

/**
 * @author loreti
 *
 */
public class SimulationBatch {

	public static double TRANSMISSION_RATE = 15.0;
	
	public static double SENDIND_RATE = 1.0;
	
	public static double HANDLING_RATE = 1000.0;
	
	public static String T_PREFIX = "results/T";

	public static String R_PREFIX = "results/R";

	public static String C_PREFIX = "results/C";

	public static double simulationTime = Double.POSITIVE_INFINITY;//30000;
	public static int samples = 100;
	public static int replications = 100;

	
	public static void runTreeSimulation( int levels , int children , int agents , float p ) throws FileNotFoundException {
//		TreeStructureFactory factory = new TreeStructureFactory(3,5,5,10, (x,y) -> 15.0 , x -> 1000.0 , x -> 1.0 );
		P2PStructureFactory factory = new P2PStructureFactory(levels,children,agents,p, (x,y) -> TRANSMISSION_RATE , x -> HANDLING_RATE , x -> SENDIND_RATE );
		SimulationEnvironment<AbCSystem> env = new SimulationEnvironment<>(factory);
		StatisticSampling<AbCSystem> averageDeliveryTime = new StatisticSampling<>(samples, simulationTime/samples, new AverageDeliveryTime());
		StatisticSampling<AbCSystem> maxDeliveryTime = new StatisticSampling<>(samples, simulationTime/samples, new MaxDeliveryTime());
		StatisticSampling<AbCSystem> minDeliveryTime = new StatisticSampling<>(samples, simulationTime/samples, new MinDeliveryTime());
		StatisticSampling<AbCSystem> numberOfDeliveredMessages = new StatisticSampling<>(samples, simulationTime/samples, new NumberOfDeliveredMessages());
//		StatisticSampling<AbCSystem> averageWaitingSize = new StatisticSampling<>(samples, simulationTime/samples, new AverageWaitingQueueSize());
		StatisticSampling<AbCSystem> averageTimeInterval = new StatisticSampling<>(samples, simulationTime/samples, new AverageMessageInterval());
		StatisticSampling<AbCSystem> maxTimeInterval = new StatisticSampling<>(samples, simulationTime/samples, new MaxMessageInterval());
		StatisticSampling<AbCSystem> minTimeInterval = new StatisticSampling<>(samples, simulationTime/samples, new MinMessageInterval());
		SamplingCollection<AbCSystem> collection = new SamplingCollection<>( averageDeliveryTime, maxDeliveryTime, minDeliveryTime, numberOfDeliveredMessages, averageTimeInterval,maxTimeInterval,minTimeInterval);
		
//		averageWaitingSize.printTimeSeries(System.out);
		env.setSampling(collection);
		env.simulate(replications,simulationTime);			
		saveSimulationData( T_PREFIX+"_"+levels+"_"+children+"_"+agents+"_"+p ,
			averageDeliveryTime ,
			maxDeliveryTime ,
			minDeliveryTime ,
			numberOfDeliveredMessages ,
			averageTimeInterval ,
			maxTimeInterval ,
			minTimeInterval,
			env.getAvgExecTime(),
			env.getStimDevStdExecTime()
		);
	}

	public static void runClusterSimulation( int cluster , int agents , float p ) throws FileNotFoundException {
//		TreeStructureFactory factory = new TreeStructureFactory(3,5,5,10, (x,y) -> 15.0 , x -> 1000.0 , x -> 1.0 );
		SingleServerFactory factory = new SingleServerFactory(agents,p, cluster, (x,y) -> TRANSMISSION_RATE , x -> HANDLING_RATE , x -> SENDIND_RATE );
		SimulationEnvironment<AbCSystem> env = new SimulationEnvironment<>(factory);
		StatisticSampling<AbCSystem> averageDeliveryTime = new StatisticSampling<>(samples, simulationTime/samples, new AverageDeliveryTime());
		StatisticSampling<AbCSystem> maxDeliveryTime = new StatisticSampling<>(samples, simulationTime/samples, new MaxDeliveryTime());
		StatisticSampling<AbCSystem> minDeliveryTime = new StatisticSampling<>(samples, simulationTime/samples, new MinDeliveryTime());
		StatisticSampling<AbCSystem> numberOfDeliveredMessages = new StatisticSampling<>(samples, simulationTime/samples, new NumberOfDeliveredMessages());
//		StatisticSampling<AbCSystem> averageWaitingSize = new StatisticSampling<>(samples, simulationTime/samples, new AverageWaitingQueueSize());
		StatisticSampling<AbCSystem> averageTimeInterval = new StatisticSampling<>(samples, simulationTime/samples, new AverageMessageInterval());
		StatisticSampling<AbCSystem> maxTimeInterval = new StatisticSampling<>(samples, simulationTime/samples, new MaxMessageInterval());
		StatisticSampling<AbCSystem> minTimeInterval = new StatisticSampling<>(samples, simulationTime/samples, new MinMessageInterval());
		SamplingCollection<AbCSystem> collection = new SamplingCollection<>( averageDeliveryTime, maxDeliveryTime, minDeliveryTime, numberOfDeliveredMessages, averageTimeInterval,maxTimeInterval,minTimeInterval);
		
//		averageWaitingSize.printTimeSeries(System.out);
		env.setSampling(collection);
		env.simulate(replications,simulationTime);			
		saveSimulationData( C_PREFIX+"_"+cluster+"_"+agents+"_"+p ,
			averageDeliveryTime ,
			maxDeliveryTime ,
			minDeliveryTime ,
			numberOfDeliveredMessages ,
			averageTimeInterval ,
			maxTimeInterval ,
			minTimeInterval,
			env.getAvgExecTime(),
			env.getStimDevStdExecTime()
		);
	}

	public static void runRingSimulation( int elements , int agents , float p ) throws FileNotFoundException {
//		TreeStructureFactory factory = new TreeStructureFactory(3,5,5,10, (x,y) -> 15.0 , x -> 1000.0 , x -> 1.0 );
		RingStructureFactory factory = new RingStructureFactory(elements,agents,p, (x,y) -> TRANSMISSION_RATE , x -> HANDLING_RATE , x -> SENDIND_RATE );
		SimulationEnvironment<AbCSystem> env = new SimulationEnvironment<>(factory);
		StatisticSampling<AbCSystem> averageDeliveryTime = new StatisticSampling<>(samples, simulationTime/samples, new AverageDeliveryTime());
		StatisticSampling<AbCSystem> maxDeliveryTime = new StatisticSampling<>(samples, simulationTime/samples, new MaxDeliveryTime());
		StatisticSampling<AbCSystem> minDeliveryTime = new StatisticSampling<>(samples, simulationTime/samples, new MinDeliveryTime());
		StatisticSampling<AbCSystem> numberOfDeliveredMessages = new StatisticSampling<>(samples, simulationTime/samples, new NumberOfDeliveredMessages());
//		StatisticSampling<AbCSystem> averageWaitingSize = new StatisticSampling<>(samples, simulationTime/samples, new AverageWaitingQueueSize());
		StatisticSampling<AbCSystem> averageTimeInterval = new StatisticSampling<>(samples, simulationTime/samples, new AverageMessageInterval());
		StatisticSampling<AbCSystem> maxTimeInterval = new StatisticSampling<>(samples, simulationTime/samples, new MaxMessageInterval());
		StatisticSampling<AbCSystem> minTimeInterval = new StatisticSampling<>(samples, simulationTime/samples, new MinMessageInterval());
		SamplingCollection<AbCSystem> collection = new SamplingCollection<>();// averageDeliveryTime, maxDeliveryTime, minDeliveryTime, numberOfDeliveredMessages, averageTimeInterval,maxTimeInterval,minTimeInterval);
		
//		averageWaitingSize.printTimeSeries(System.out);
		env.setSampling(collection);
		env.simulate(replications,simulationTime);			
		saveSimulationData( R_PREFIX+"_"+elements+"_"+agents+"_"+p ,
			averageDeliveryTime ,
			maxDeliveryTime ,
			minDeliveryTime ,
			numberOfDeliveredMessages ,
			averageTimeInterval ,
			maxTimeInterval ,
			minTimeInterval,
			env.getAvgExecTime(),
			env.getStimDevStdExecTime()
		);
	}

	
	private static void saveSimulationData(String name, StatisticSampling<AbCSystem> averageDeliveryTime,
			StatisticSampling<AbCSystem> maxDeliveryTime, StatisticSampling<AbCSystem> minDeliveryTime,
			StatisticSampling<AbCSystem> numberOfDeliveredMessages, StatisticSampling<AbCSystem> averageTimeInterval,
			StatisticSampling<AbCSystem> maxTimeInterval, StatisticSampling<AbCSystem> minTimeInterval,
			double avgExecTime, double stimDevStdExecTime) throws FileNotFoundException {
		/*doSave( name+"_avg_dt_.dat",averageDeliveryTime);
		doSave( name+"_min_dt_.dat",minDeliveryTime);
		doSave( name+"_max_dt_.dat",maxDeliveryTime);
		doSave( name+"_avg_ti_.dat",averageTimeInterval);
		doSave( name+"_min_ti_.dat",minTimeInterval);
		doSave( name+"_max_ti_.dat",maxTimeInterval);
		doSave( name+"_dm_.dat",numberOfDeliveredMessages);*/
		
		File execTimeFile = new File(name+"_exec_time.txt");
		PrintStream pw = new PrintStream(execTimeFile);
		double deltaET = stimDevStdExecTime * 1.96;
		pw.println("" + avgExecTime + "\t" + (-deltaET)+"\t"+deltaET);
		pw.close();
	}


	private static void doSave(String string, StatisticSampling<AbCSystem> stat) throws FileNotFoundException {
		File file = new File(string);
		PrintStream pw = new PrintStream(file);
		stat.getSimulationTimeSeries(replications).get(0).printTimeSeries(pw);
	}

	public static void batch(int k, boolean sparse, float p) throws FileNotFoundException {
		int n = 31*k;
		String density = sparse?"sparse":"dense"; 
		System.out.println("C[10,"+n+"] "+density);
		runClusterSimulation(10, n, p);
		System.out.println("C[20,"+n+"] "+density);
		runClusterSimulation(20, n, p);
		System.out.println("C[31,"+n+"] "+density);
		runClusterSimulation(31, n, p);
		System.out.println("R["+k+",31]"+density);
		runRingSimulation(k, 31, p);
		System.out.println("R[31,"+k+"]"+density);
		runRingSimulation(31, k, p);
		System.out.println("T[5,2,"+k+"]"+density);
		runTreeSimulation(5, 2, k, p);
		System.out.println("T[3,5,"+k+"]"+density);
		runTreeSimulation(3, 5, k, p);
	}
	
	public static void main(String[] argv) throws FileNotFoundException {
		int[] ks = new int[] {2, 7, 9};
		float[] sparse = new float[] {0.01f, 0.005f, 0.005f};
		float[] dense  = new float[] {0.1f,  0.05f,  0.05f};
		for(int i=0; i<ks.length; i++) {
			batch(ks[i], true, sparse[i]);
			batch(ks[i], false, dense[i]);
		}
	}

	public static void mainz(String[] argv) throws FileNotFoundException {
		float sparse155 = 0.01f,  dense155 = 0.1f,
			  sparse310 = 0.005f, dense310 = 0.05f;
			  //sparse620 = 0.005f, dense620 = 0.05f;
		System.out.println("C[10,155] sparse");
		runClusterSimulation(10, 155, sparse155);
		System.out.println("C[10,155] dense");
		runClusterSimulation(10, 155, dense155);
		System.out.println("C[10,310] dense");
		runClusterSimulation(10, 310, dense310);
		System.out.println("C[10,310] sparse");
		runClusterSimulation(10, 310, sparse310);
		//System.out.println("C[10,620] dense");
		//runClusterSimulation(10, 620, dense620);
		System.out.println("C[20,155] dense");
		runClusterSimulation(20, 155, dense155);
		System.out.println("C[20,155] sparse");
		runClusterSimulation(20, 155, sparse155);
		System.out.println("C[20,310] dense");
		runClusterSimulation(20, 310, dense310);
		System.out.println("C[20,310] sparse");
		runClusterSimulation(20, 310, sparse310);
		//System.out.println("C[20,620] dense");
		//runClusterSimulation(20, 620, dense620);
		System.out.println("C[31,155] dense");
		runClusterSimulation(31, 155, dense155);
		System.out.println("C[31,155] sparse");
		runClusterSimulation(31, 155, sparse155);
		System.out.println("C[31,310] dense");
		runClusterSimulation(31, 310, dense310);
		System.out.println("C[31,310] sparse");
		runClusterSimulation(31, 310, sparse310);
		//System.out.println("C[31,620] dense");
		//runClusterSimulation(31, 620, dense620);
		/*System.out.println("C[10,155]");
		runClusterSimulation(10, 155, sparse155);
		System.out.println("C[10,310]");
		runClusterSimulation(10, 310, sparse310);
		//System.out.println("C[10,620]");
		//runClusterSimulation(10, 620, sparse620);
		System.out.println("C[20,155]");
		runClusterSimulation(20, 155, sparse155);
		System.out.println("C[20,310]");
		runClusterSimulation(20, 310, sparse310);
		//System.out.println("C[20,620]");
		//runClusterSimulation(20, 620, sparse620);
		System.out.println("C[31,155]");
		runClusterSimulation(31, 155, sparse155);
		System.out.println("C[31,310]");
		runClusterSimulation(31, 310, sparse310);*/
		//System.out.println("C[31,620]");
		//runClusterSimulation(31, 620, sparse620);
		System.out.println("R[5,31] dense");
		runRingSimulation(5, 31, dense155);
		System.out.println("R[5,31] sparse");
		runRingSimulation(5, 31, sparse155);
		System.out.println("R[5,62] dense");
		runRingSimulation(5, 62, dense310);
		System.out.println("R[5,62] sparse");
		runRingSimulation(5, 62, sparse310);
		//System.out.println("R[5,124]");
		//runRingSimulation(5, 124, dense620);
		System.out.println("R[10,31] dense");
		runRingSimulation(10, 31, dense310);
		System.out.println("R[10,31] sparse");
		runRingSimulation(10, 31, sparse310);
		//System.out.println("R[10,62]");
		//runRingSimulation(10, 62, dense620);
		//System.out.println("R[10,31]");
		//runRingSimulation(10, 31, -1);
		//System.out.println("R[20,31]");
		//runRingSimulation(20, 31, dense620);
		System.out.println("R[31,5] dense");
		runRingSimulation(31, 5, dense155);
		System.out.println("R[31,5] sparse");
		runRingSimulation(31, 5, sparse155);
		System.out.println("R[31,10] dense");
		runRingSimulation(31, 10, dense310);
		System.out.println("R[31,10] sparse");
		runRingSimulation(31, 10, sparse310);
		//System.out.println("R[31,20]");
		//runRingSimulation(31, 20, dense620);
		/*System.out.println("R[10,31]");
		runRingSimulation(31, 5, 15);
		System.out.println("R[10,31]");
		runRingSimulation(31, 10, 31);
		System.out.println("R[10,31]");
		runRingSimulation(62, 5, 31);
		System.out.println("R[10,31]");
		runRingSimulation(31, 20, 62);
		System.out.println("R[10,31]");
		runRingSimulation(62, 10, 62);
		System.out.println("R[10,31]");
		runRingSimulation(128, 5, 62);*/
		System.out.println("T[5,2,5] dense");
		runTreeSimulation(5, 2, 5, dense155);
		System.out.println("T[5,2,5] sparse");
		runTreeSimulation(5, 2, 5, sparse155);
		System.out.println("T[5,2,10] dense");
		runTreeSimulation(5, 2, 10, dense310);
		System.out.println("T[5,2,10] sparse");
		runTreeSimulation(5, 2, 10, sparse310);
		//System.out.println("T[5,2,20]");
		//runTreeSimulation(5, 2, 20, dense620);
		System.out.println("T[3,5,5] dense");
		runTreeSimulation(3, 5, 5, dense155);
		System.out.println("T[3,5,5] sparse");
		runTreeSimulation(3, 5, 5, sparse155);
		System.out.println("T[3,5,10] dense");
		runTreeSimulation(3, 5, 10, dense310);
		System.out.println("T[3,5,10] sparse");
		runTreeSimulation(3, 5, 10, sparse310);
		//System.out.println("T[3,5,10]");
		//runTreeSimulation(3, 5, 20, dense620);
		/*runTreeSimulation(5, 2, 5, 15);
		runTreeSimulation(5, 2, 10, 31);
		runTreeSimulation(5, 2, 20, 62);
		runTreeSimulation(3, 5, 5, 15);
		runTreeSimulation(3, 5, 10, 31);
		runTreeSimulation(3, 5, 20, 62);*/
	}
	
}
