package com.mc.saas.offer.picker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.mc.saas.offer.picker.utils.ConfigHelper;
import com.mc.saas.offer.picker.utils.GlobalDataSetsHelper;
import com.mc.saas.offer.picker.utils.MetricProcessor;
import com.mc.saas.offer.picker.utils.OfferDataSets;
import com.mc.saas.offer.picker.utils.OfferMetrics;
import com.mc.saas.offer.picker.utils.PersistHelper;
import com.mc.saas.offer.picker.utils.WorkerMetrics;
/**
 * @author suqiang.song
 *
 */
public class OfferPickerDPMain {

	static int offerCount = 30;
	static int userCount = 60034;
	static int userGroupCount = 1000;
	static int maxOffer = 30;
	static int minOffer = 10;
	static double diverseWeight = 0.5d;
	static boolean parallelExecution = false;
	static boolean persist = false;
	static String fi = "testFI";
	static String dataSource = "simulation";
	static Queue<Integer> weightQueue = new PriorityQueue<>();
	static List<String> weightOrderList = new ArrayList<String>();
	static Map<Integer, AtomicInteger> offerCounter = new HashMap<Integer, AtomicInteger>();
	static Map<Integer,List<Knapsack>> rawOfferUserBags = new HashMap<Integer,List<Knapsack>>();

	static Gson gson = new Gson();
	private static final Logger log = LoggerFactory.getLogger(OfferPickerDPMain.class);

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		if (args.length == 1 && null != args[0] && args[0].contains(".properties")) {
			ConfigHelper.getInstance().init(args[0]);
		} else {
			log.warn("There is no external Properties file , will use all default configuations !");
		}

		offerCount = Integer.parseInt(ConfigHelper.getInstance().getProperty("offerCount"));
		userCount = Integer.parseInt(ConfigHelper.getInstance().getProperty("userCount"));
		userGroupCount = Integer.parseInt(ConfigHelper.getInstance().getProperty("userGroupCount"));
		maxOffer = Integer.parseInt(ConfigHelper.getInstance().getProperty("maxOfferPerUser"));
		minOffer = Integer.parseInt(ConfigHelper.getInstance().getProperty("minOfferPerUser"));
		diverseWeight = Double.parseDouble(ConfigHelper.getInstance().getProperty("diverseWeight"));
		parallelExecution = Boolean.valueOf(ConfigHelper.getInstance().getProperty("parallelExecution"));
		persist = Boolean.valueOf(ConfigHelper.getInstance().getProperty("persist"));
		fi = ConfigHelper.getInstance().getProperty("fi");
		dataSource = ConfigHelper.getInstance().getProperty("dataSource");
		loadUserOfferScores();
		constructPickingDataSets();
		doOfferPicking();
		long end = System.currentTimeMillis();
		MetricProcessor.getInstance().addMetrics(new WorkerMetrics("totalTime", (end - start)));
		cleanAndExit();
	}

	public static void cleanAndExit() {
		PersistHelper.getInstance().clean();
		GlobalDataSetsHelper.getInstance().clean();
		MetricProcessor.getInstance().quit();
	}

	public static OfferPickingCalculator createPickingCalculator(int offerId, int bagSize, int totalWeight) {
		GlobalDataSetsHelper.getInstance().getOfferMatrixsMap().put(offerId, new int[bagSize][totalWeight + 1]);
		return new OfferPickingCalculator(offerId, userCount, totalWeight,
				GlobalDataSetsHelper.getInstance().getUserGroupOfferBags().get(offerId),
				GlobalDataSetsHelper.getInstance().getOfferMatrixsMap().get(offerId));
	}

	public static OfferPickingSolver createPickingSolver(int offerId, int totalWeight,
			ConcurrentHashMap<Integer, KnapsackConstraint> userConstraints) {
		return new OfferPickingSolver(offerId, userGroupCount,
				GlobalDataSetsHelper.getInstance().getOfferQuotas().get(offerId),
				GlobalDataSetsHelper.getInstance().getOfferPriorities().get(offerId),
				GlobalDataSetsHelper.getInstance().getUserGroupOfferBags().get(offerId), userConstraints,
				GlobalDataSetsHelper.getInstance().getOfferMatrixsMap().get(offerId), totalWeight);
	}

	public static void loadUserOfferScores() {

		if (dataSource.equals("simulation")) {
			log.info("Simulating the user offer scores");
			simulateRawBags();
		} else {
			log.info("load raw csv file for user offer scores");
		}
	}

	public static void generateWeights() {
		weightQueue.clear();
		for (int i = 0; i < offerCount; i++) {
			weightQueue.add(ThreadLocalRandom.current().nextInt(1, 9998 + 1));
		}
		weightOrderList.clear();
		for (int j = 1; j < offerCount + 1; j++) {
			weightOrderList.add(j + "," + weightQueue.poll());
		}
		Collections.shuffle(weightOrderList);
	}
	
	
	public static void initOfferCounter()
	{
		for (int i = 1; i < offerCount + 1; i++)
		{
			offerCounter.put(i,new AtomicInteger(0));
		}
	}
	
	private static void initOfferUserBags() {
		for (int i = 1; i < offerCount + 1; i++)
		{
			ArrayList<Knapsack> tmpList = new ArrayList<Knapsack>();
			rawOfferUserBags.put(i, tmpList);
		}
		
	}

	public static void simulateRawBags() {
		long startTime = System.currentTimeMillis();
		// init the counter 
		initOfferCounter();
		// init the OfferUserBags 
		initOfferUserBags();
		// generate the all user bags with weights and values, group by userId but increment the offerCounter
		for(int i=1;i<userCount+1;i++)
		{
			generateWeights();
			int currentOfferId=1;
			for(String wo:weightOrderList)
			{	
				offerCounter.get(currentOfferId).incrementAndGet();
				rawOfferUserBags.get(currentOfferId).add(new Knapsack(i, 0,currentOfferId, "m"+currentOfferId, Integer.parseInt(wo.split(",")[0]),Integer.parseInt(wo.split(",")[1])));
				//add it to OfferUserBags
				currentOfferId++;	
			}
		}
		// assign rawOfferUserBags to GlobalDataSetsHelper
		Set<Integer> keySet = rawOfferUserBags.keySet();
		Iterator<Integer> keySetIterator = keySet.iterator();
		while (keySetIterator.hasNext()) {
		   
		   int offerID = keySetIterator.next();
		   // sort , order by value 
		   Collections.sort(rawOfferUserBags.get(offerID));
		   Knapsack[] tmpBagArray = rawOfferUserBags.get(offerID).toArray(new Knapsack[offerCounter.get(offerID).intValue()]);
		   GlobalDataSetsHelper.getInstance().getUserOfferBags().put(offerID, tmpBagArray);
		   rawOfferUserBags.get(offerID).clear();
		}
		rawOfferUserBags.clear();
		
		long endTime = System.currentTimeMillis();
		MetricProcessor.getInstance().addMetrics(new WorkerMetrics("simulateRawBags", (endTime - startTime)));
	}




	public static void constructPickingDataSets() {
		long start = System.currentTimeMillis();

		if (!parallelExecution) {
			log.info("run offer picker constructPickingDataSets by offer sequrence");
			for (int i = 1; i < offerCount + 1; i++) {
				OfferPickingConstructor opc = new OfferPickingConstructor(i, offerCount, userCount, userGroupCount,
						diverseWeight);
				OfferDataSets ods = opc.offerPickingConstruct();
				GlobalDataSetsHelper.getInstance().getUserOfferBags().put(i, ods.getUserOfferBags());
				GlobalDataSetsHelper.getInstance().getUserGroupOfferBags().put(i, ods.getUserGroupOfferBags());
				GlobalDataSetsHelper.getInstance().getOfferPriorities().put(i, ods.getOfferPriority());
				GlobalDataSetsHelper.getInstance().getOfferQuotas().put(i, ods.getOfferQuota());
				if (persist) {
					// add result file
					PersistHelper.getInstance().registerWriter("OfferResult" + i,
							ConfigHelper.getInstance().getProperty("rootPath") + fi + File.separator + "result"
									+ File.separator,
							"_result.csv");
				}
			}
		} else {
			log.info("run offer picker constructPickingDataSets in parallel");
			ExecutorService offerPickingConstructorExecutorService = Executors.newCachedThreadPool();
			List<Future<OfferDataSets>> offerPickingConstructorResultList = new ArrayList<Future<OfferDataSets>>();
			for (int i = 1; i < offerCount + 1; i++) {
				Future<OfferDataSets> future = offerPickingConstructorExecutorService
						.submit(new OfferPickingConstructor(i, offerCount, userCount, userGroupCount, diverseWeight));
				offerPickingConstructorResultList.add(future);
				if (persist) {
					// add result file
					PersistHelper.getInstance().registerWriter("OfferResult" + i,
							ConfigHelper.getInstance().getProperty("rootPath") + fi + File.separator + "result"
									+ File.separator,
							"_result.csv");
				}
			}

			for (Future<OfferDataSets> fs : offerPickingConstructorResultList) {
				try {
					while (!fs.isDone())
						;

					OfferDataSets ods = fs.get();
					GlobalDataSetsHelper.getInstance().getUserOfferBags().put(ods.getOfferId(), ods.getUserOfferBags());
					GlobalDataSetsHelper.getInstance().getUserGroupOfferBags().put(ods.getOfferId(),
							ods.getUserGroupOfferBags());
					GlobalDataSetsHelper.getInstance().getOfferPriorities().put(ods.getOfferId(),
							ods.getOfferPriority());
					GlobalDataSetsHelper.getInstance().getOfferQuotas().put(ods.getOfferId(), ods.getOfferQuota());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} finally {
					offerPickingConstructorExecutorService.shutdown();
				}
			}
		}
		// prepare global UserConstraints and UserGroupConstraints
		prepareConstraints();
		if (persist) {
			// add offer evalute file
			PersistHelper.getInstance().registerWriter("OfferEvaluate",
					ConfigHelper.getInstance().getProperty("rootPath") + fi + File.separator + "evalute"
							+ File.separator,
					"_eva.csv");
			// add user group evalute file
			PersistHelper.getInstance().registerWriter("UserGroupEvaluate",
					ConfigHelper.getInstance().getProperty("rootPath") + fi + File.separator + "evalute"
							+ File.separator,
					"_eva.csv");
			// add user evalute file
			PersistHelper.getInstance().registerWriter("UserEvaluate",
					ConfigHelper.getInstance().getProperty("rootPath") + fi + File.separator + "evalute"
							+ File.separator,
					"_eva.csv");

		}
		long end = System.currentTimeMillis();
		MetricProcessor.getInstance().addMetrics(new WorkerMetrics("simulateAndInit", (end - start)));

	}

	public static void doCalculation() {
		long start = System.currentTimeMillis();
		if (!parallelExecution) {
			log.info("run offer picker calculation by offer sequrence");
			for (int i = 1; i < offerCount + 1; i++) {
				log.info(" OfferId:" + i + " offerpriority: "
						+ GlobalDataSetsHelper.getInstance().getOfferPriorities().get(i));
				int totalWeight = (int) (offerCount * GlobalDataSetsHelper.getInstance().getOfferQuotas().get(i) * 1);
				OfferPickingCalculator opc = createPickingCalculator(i,
						GlobalDataSetsHelper.getInstance().getUserGroupOfferBags().get(i).length + 1, totalWeight);
				log.info(opc.offerPickingCalculation());
			}
		} else {
			log.info("run offer picker calculation by offer priority in parallel");
			ExecutorService offerPickingCalculatorExecutorService = Executors.newCachedThreadPool();
			List<Future<String>> offerPickingCalculatorResultList = new ArrayList<Future<String>>();
			for (int i = 1; i < offerCount + 1; i++) {
				log.info(" OfferId:" + i + " offerpriority: "
						+ GlobalDataSetsHelper.getInstance().getOfferPriorities().get(i));
				int totalWeight = (int) (offerCount * GlobalDataSetsHelper.getInstance().getOfferQuotas().get(i)
						* GlobalDataSetsHelper.getInstance().getOfferPriorities().get(i));
				Future<String> future = offerPickingCalculatorExecutorService.submit(createPickingCalculator(i,
						GlobalDataSetsHelper.getInstance().getUserGroupOfferBags().get(i).length + 1, totalWeight));
				offerPickingCalculatorResultList.add(future);
			}

			for (Future<String> fs : offerPickingCalculatorResultList) {
				try {
					while (!fs.isDone())
						;
					log.info(fs.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} finally {
					offerPickingCalculatorExecutorService.shutdown();
				}
			}
		}

		long end = System.currentTimeMillis();
		MetricProcessor.getInstance().addMetrics(new WorkerMetrics("doCalculation", (end - start)));
	}

	public static void doSolve() {
		if (!parallelExecution) {
			log.info("run offer picker solve by offer sequrence");
			for (int i = 1; i < offerCount + 1; i++) {
				int totalWeight = (int) (offerCount * GlobalDataSetsHelper.getInstance().getOfferQuotas().get(i) * 1);
				OfferPickingSolver solver = createPickingSolver(i, totalWeight,
						GlobalDataSetsHelper.getInstance().getUserGroupConstraints());
				GlobalDataSetsHelper.getInstance().getOmList()
						.add(gson.fromJson(solver.offerPickingSolve(), OfferMetrics.class));
			}
		} else {
			log.info("run offer picker solve by offer priority in parallel");
			ExecutorService offerPickingSolverExecutorService = Executors.newCachedThreadPool();
			List<Future<String>> offerPickingSolveResultList = new ArrayList<Future<String>>();
			for (int i = 1; i < offerCount + 1; i++) {
				int totalWeight = (int) (offerCount * GlobalDataSetsHelper.getInstance().getOfferQuotas().get(i)
						* GlobalDataSetsHelper.getInstance().getOfferPriorities().get(i));
				Future<String> future = offerPickingSolverExecutorService.submit(createPickingSolver(i, totalWeight,
						GlobalDataSetsHelper.getInstance().getUserGroupConstraints()));
				offerPickingSolveResultList.add(future);
			}

			for (Future<String> fs : offerPickingSolveResultList) {
				try {
					while (!fs.isDone())
						;
					String ret = fs.get();
					if (null == ret) {
						log.warn("NULL output generated !");
					} else {
						GlobalDataSetsHelper.getInstance().getOmList().add(gson.fromJson(fs.get(), OfferMetrics.class));
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} finally {
					offerPickingSolverExecutorService.shutdown();
				}
			}
		}
	}

	public static void doAssignment() {
		long start = System.currentTimeMillis();
		Map<Integer, Knapsack[]> map = new TreeMap<Integer, Knapsack[]>(
				GlobalDataSetsHelper.getInstance().getUserOfferBags());
		log.debug("doAssignment,Sorting by offerid+user id:");
		Set<Entry<Integer, Knapsack[]>> set = map.entrySet();
		Iterator<Entry<Integer, Knapsack[]>> iterator = set.iterator();

		if (!parallelExecution) {
			log.info("run offer picker assignment by offer sequrence");

			while (iterator.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry me = (Map.Entry) iterator.next();
				OfferPickingAssigner assigner = new OfferPickingAssigner((int) me.getKey(),
						GlobalDataSetsHelper.getInstance().getUserOfferBags().get(me.getKey()),
						GlobalDataSetsHelper.getInstance().getUserGroupConstraints(),
						GlobalDataSetsHelper.getInstance().getUserConstraints());
				OfferDataSets ods = assigner.offerPickingAssignment();
				// do save()
				OfferPickingSaver saver = new OfferPickingSaver(ods, persist);
				saver.doSave();

			}

		} else {
			log.info("run offer picker assignment by offer priority in parallel");
			ExecutorService offerPickingAssignExecutorService = Executors.newCachedThreadPool();
			List<Future<OfferDataSets>> offerPickingAssignResultList = new ArrayList<Future<OfferDataSets>>();
			List<OfferDataSets> resultList = new ArrayList<OfferDataSets>();
			while (iterator.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry me = (Map.Entry) iterator.next();
				Future<OfferDataSets> future = offerPickingAssignExecutorService.submit(new OfferPickingAssigner(
						(int) me.getKey(), GlobalDataSetsHelper.getInstance().getUserOfferBags().get(me.getKey()),
						GlobalDataSetsHelper.getInstance().getUserGroupConstraints(),
						GlobalDataSetsHelper.getInstance().getUserConstraints()));
				offerPickingAssignResultList.add(future);

			}
			for (Future<OfferDataSets> fs : offerPickingAssignResultList) {
				try {
					while (!fs.isDone())
						;
					resultList.add(fs.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} finally {
					offerPickingAssignExecutorService.shutdown();
				}
			}

			// do save()
			ExecutorService offerPickingSaverExecutorService = Executors.newCachedThreadPool();
			List<Future<String>> offerPickingSaverResultList = new ArrayList<Future<String>>();
			for (OfferDataSets ods : resultList) {
				Future<String> future = offerPickingSaverExecutorService.submit(new OfferPickingSaver(ods, persist));
				offerPickingSaverResultList.add(future);
			}

			for (Future<String> fs : offerPickingSaverResultList) {
				try {
					while (!fs.isDone())
						;
					log.info(fs.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} finally {
					offerPickingSaverExecutorService.shutdown();
				}
			}

		}

		long end = System.currentTimeMillis();
		MetricProcessor.getInstance().addMetrics(new WorkerMetrics("doAssignment", (end - start)));
	}

	public static void doOfferPicking() {
		long start = System.currentTimeMillis();
		doControlGroup();
		doCalculation();
		doSolve();
		doAssignment();
		doEvaluate();
		long end = System.currentTimeMillis();
		MetricProcessor.getInstance().addMetrics(new WorkerMetrics("doOfferPicking", (end - start)));
	}

	private static void doEvaluate() {
		OfferPickingEvaluator evaluator = new OfferPickingEvaluator();
		evaluator.offerPickingEvaluate();
	}

	private static void doControlGroup() {
		// TODO Auto-generated method stub
	}

	public static void prepareConstraints() {
		// construct empty user group and user Constraints
		for (int i = 0; i < userGroupCount; i++) {
			GlobalDataSetsHelper.getInstance().getUserGroupConstraints().put(i,
					new KnapsackConstraint(maxOffer, minOffer));
		}
		for (int j = 1; j < userCount + 1; j++) {
			GlobalDataSetsHelper.getInstance().getUserConstraints().put(j, new KnapsackConstraint(maxOffer, minOffer));
		}
	}
}