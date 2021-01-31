package qbfsolver;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		final ExecutorService service = Executors.newSingleThreadExecutor();
        try {
                final Future<Object> f = service.submit(() -> {
            	QdimacFileReader rd = new QdimacFileReader();
        		CnfExpression fo = rd.read();
        		CmdArgs arg = ResultGenerator.getCommandLine();
        		Solver s = new DeepPNS();
        		if (args.length >= 1) {
        			int val = Integer.valueOf(args[0]);
        			if (val == 0) {
        				System.out.println("using brute force");
        				s = new BruteForce();
        			} else if (val == 1) {
        				System.out.println("using PNS return to the root with standard intialization");
        				s = new PNS();
        				arg.setType(1);
        			} else if (val == 2) {
        				System.out.println("using PNS with stack with standard intialization");
        				s = new PNSv2();
        				arg.setType(2);
        			} else if (val == 3) {
        				System.out.println("using standard PNS with mobility intialization");
        				s = new PNS();
        				arg.setType(3);
        			} else if (val == 4) {
        				s = new PNSv2();
        				arg.setType(3);
        				System.out.println("using PNS with stack with mobility intialization");
        			} else {
        				arg.setType(3);
        				System.out.println("using DeepPNS with stack with mobility intialization");
        			}
        			
        			if (args.length >= 3) {
        				arg.setBfE(Integer.valueOf(args[1]));
        				arg.setBfU(Integer.valueOf(args[2]));
        			}
        			
        		} else {
        			System.out.println("using PNS with stack with mobility intialization");
        			arg.setType(0);
        		}
        		
        		System.out.println("Max Branching factor Existential " + (1 << arg.getBfE()));
        		System.out.println("Max Branching factor Universal " + (1 << arg.getBfU()));
        		boolean res = s.solve(fo);
        		if (s.getClass() == BruteForce.class) {
        			Result ret = ResultGenerator.getInstance();
        			ret.setTruth(res);
        			ret.setIteration(0);
        		}
        		
        		System.out.println(res);
        		return ResultGenerator.getInstance();
            });

            System.out.println(f.get(900, TimeUnit.SECONDS));
            
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
