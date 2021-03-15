package qbfsolver;

import java.util.concurrent.*;

public class Main {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		final ExecutorService service = Executors.newSingleThreadExecutor();
        try {
                final Future<Object> f = service.submit(() -> {
            	QdimacFileReader rd = new QdimacFileReader();
        		CnfExpression fo;
        		Result ret = ResultGenerator.getInstance();
        		if (args.length == 0) {
        			fo = rd.read(0);
        		    BruteForce s = new BruteForce();
        			//DeepPNS s = new DeepPNS();
        			System.out.println("bruteforce with persistent data structure");
        			ret.setTruth(s.solve(fo));
        			//s.solve(fo);
        		} else {
        			fo = rd.read(1);
        			System.out.println(fo.getClass());
        			//BruteForce s = new BruteForce();
        			DeepPNS s = new DeepPNS();
        			s.solve(fo);
        			//ret.setTruth(s.solve_copy(fo));
        		}	
        		return ResultGenerator.getInstance();
            });

            System.out.println(f.get(200000, TimeUnit.SECONDS));
            
        } catch (final TimeoutException e) {
            System.out.println("UNSOLVED NA");
            service.shutdown();
            System.exit(0);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
        	service.shutdown();
        }

	}

}
