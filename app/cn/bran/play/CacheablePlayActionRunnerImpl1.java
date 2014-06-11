package cn.bran.play;

public class CacheablePlayActionRunnerImpl1 extends CacheablePlayActionRunner{
	RunAction runAction;
	
	public RunAction getRunAction() {
		return this.runAction;
	}

	public void setRunAction(RunAction _runAction) {
		this.runAction = _runAction;
	}

	public CacheablePlayActionRunnerImpl1(String ttl) {
		super(ttl);
	}
	
	public CacheablePlayActionRunnerImpl1(String ttl, Object... args) {
		super(ttl, args);
	}

	@Override
	protected JapidResult runPlayAction() {
		return this.runAction.runPlayAction();
	}

}
