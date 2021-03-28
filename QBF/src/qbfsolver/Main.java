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
        		    DeepPNS s = new DeepPNS();
        			s.solve(fo);
        		} else {
        			fo = rd.read(0);
        			System.out.println(fo.getClass());
        			BruteForce s = new BruteForce();
        			ret.setTruth(s.solve(fo));
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
