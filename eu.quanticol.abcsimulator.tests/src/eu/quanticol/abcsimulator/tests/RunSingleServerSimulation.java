/**
 * 
 */
package eu.quanticol.abcsimulator.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;

import org.cmg.ml.sam.sim.SimulationEnvironment;
import org.cmg.ml.sam.sim.sampling.SamplingCollection;
import org.cmg.ml.sam.sim.sampling.SimulationTimeSeries;
import org.cmg.ml.sam.sim.sampling.StatisticSampling;

import eu.quanticol.abcsimulator.AbCSystem;
import eu.quanticol.abcsimulator.AverageDeliveryTime;
import eu.quanticol.abcsimulator.AverageInputQueueSize;
import eu.quanticol.abcsimulator.AverageWaitingQueueSize;
import eu.quanticol.abcsimulator.MaxDeliveryTime;
import eu.quanticol.abcsimulator.MinDeliveryTime;
import eu.quanticol.abcsimulator.NumberOfDeliveredMessages;
import eu.quanticol.abcsimulator.SingleServerFactory;
import eu.quanticol.abcsimulator.TravellingMessages;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.IXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.ErrorBarType;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.swt.widgets.Display;

import org.eclipse.swt.widgets.Shell;
/**
 * @author loreti
 *
 */
public class RunSingleServerSimulation {

	public static void main(String[] argv) throws FileNotFoundException {
		final Shell shell = new Shell();
		shell.setSize(600, 550);
		shell.open();

//		// use LightweightSystem to create the bridge between SWT and draw2D
		final LightweightSystem lws = new LightweightSystem(shell);
//
//		// create a new XY Graph.
		IXYGraph xyGraph = new XYGraph();
		ToolbarArmedXYGraph toolbarArmedXYGraph = new ToolbarArmedXYGraph(xyGraph);
		xyGraph.setTitle("Performance Evaluation of the Centralized Infrastructure");
//		// set it as the content of LightwightSystem
		lws.setContents(toolbarArmedXYGraph);
//
//		// Configure XYGraph
				xyGraph.getPrimaryXAxis().setShowMajorGrid(true);
				xyGraph.getPrimaryYAxis().setShowMajorGrid(true);
				xyGraph.getPrimaryXAxis().setAutoScale(true);
				xyGraph.getPrimaryYAxis().setAutoScale(true);
		FileOutputStream fout=new FileOutputStream("mfile.txt");
		double simulationTime = 5000;
		int samples = 10000;
		int replications = 100;
		SingleServerFactory factory = new SingleServerFactory(160,1, (x,y) -> 10.0 , x -> 100.0 , x -> 0.1 );
		SimulationEnvironment<AbCSystem> env = new SimulationEnvironment<>(factory);		
		StatisticSampling<AbCSystem> averageDeliveryTime = new StatisticSampling<>(samples, simulationTime/samples, new AverageDeliveryTime());
		StatisticSampling<AbCSystem> maxDeliveryTime = new StatisticSampling<>(samples, simulationTime/samples, new MaxDeliveryTime());
		StatisticSampling<AbCSystem> minDeliveryTime = new StatisticSampling<>(samples, simulationTime/samples, new MinDeliveryTime());
		StatisticSampling<AbCSystem> numberOfDeliveredMessages = new StatisticSampling<>(samples, simulationTime/samples, new NumberOfDeliveredMessages());
	
		StatisticSampling<AbCSystem> averageWaitingSize = new StatisticSampling<>(samples, simulationTime/samples, new AverageWaitingQueueSize());
//		StatisticSampling<AbCSystem> numberOfMessages = new StatisticSampling<>(2001, 0.5, new NumberOfDeliveredMessages());
//		StatisticSampling<AbCSystem> sentMessages = new StatisticSampling<>(2001, 0.5, new TravellingMessages());
		SamplingCollection<AbCSystem> collection = new SamplingCollection<>( averageDeliveryTime, maxDeliveryTime, minDeliveryTime, numberOfDeliveredMessages,averageWaitingSize);
		env.setSampling(collection);
		env.simulate(replications,simulationTime);
		//averageDeliveryTime.printTimeSeries(new PrintStream(fout));
		LinkedList<SimulationTimeSeries> series=new LinkedList<>();
		series.addAll(averageDeliveryTime.getSimulationTimeSeries(replications));
		series.addAll(maxDeliveryTime.getSimulationTimeSeries(replications));
		series.addAll(minDeliveryTime.getSimulationTimeSeries(replications));
		//series.addAll(numberOfDeliveredMessages.getSimulationTimeSeries(replications));
		//series.addAll(averageWaitingSize.getSimulationTimeSeries(replications));
		LinkedList<Trace> traces=new LinkedList<>();
		LinkedList<SimulationTrace> simtraces=new LinkedList<>();
		for (SimulationTimeSeries ser: series) {
			simtraces.add(new SimulationTrace(ser));
		}
		for (SimulationTrace strace: simtraces) {
			Trace trace = new Trace(strace.getName(), xyGraph.getPrimaryXAxis(), xyGraph.getPrimaryYAxis(), strace ) ;
			trace.setErrorBarEnabled(true);
			trace.setYErrorBarType(ErrorBarType.BOTH);
			trace.setXErrorBarType(ErrorBarType.NONE);
			xyGraph.addTrace( trace );
			traces.add(trace);
		}
		Display display = Display.getDefault();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	//	SimulationTrace simTrace=new SimulationTrace(averageDeliveryTime.getSimulationTimeSeries(10));
		//numberOfDeliveredMessages.printTimeSeries(new PrintStream(fout));;
		
	}
	
}
