/**
 * Copyright 2010 Bing Ran<bing_ran@hotmail.com> 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package cn.bran.play;

import java.util.TreeMap;

import cn.bran.japid.template.JapidTemplateBaseWithoutPlay;

/**
 * a java based template using StringBuilder as the content buffer
 * 
 * XXX: not really Play dependent. Is this class really necessary? Is 
 * the concept of action runners applicable to the general template too? If so should it be moved up the the real base?
 * 
 * @author bran
 * 
 */ 
public abstract class JapidTemplateBase extends JapidTemplateBaseWithoutPlay {
	
	private static final long serialVersionUID = 98249167751601311L;

	public JapidTemplateBase(StringBuilder out) {
		super(out);
		initme();
	}

	/**
	 * @author Bing Ran (bing.ran@gmail.com)
	 */
	private void initme() {
		if (this.actionRunners == null) {
			this.actionRunners = new TreeMap<Integer, cn.bran.japid.template.ActionRunner>();
		}
	}

	public JapidTemplateBase(JapidTemplateBaseWithoutPlay _caller) {
		super(_caller);
		if (_caller instanceof JapidTemplateBase){
			setActionRunners(((JapidTemplateBase)_caller).getActionRunners());
		}
		initme();
	}

	/**
	 * to keep track of all the action invocations by #{invoke} tag
	 */
	protected TreeMap<Integer, cn.bran.japid.template.ActionRunner> actionRunners;// = new TreeMap<Integer, cn.bran.japid.template.ActionRunner>();

	public TreeMap<Integer, cn.bran.japid.template.ActionRunner> getActionRunners() {
		return this.actionRunners;
	}

	public JapidTemplateBaseWithoutPlay setActionRunners(
			TreeMap<Integer, cn.bran.japid.template.ActionRunner> _actionRunners) {
		this.actionRunners = _actionRunners;
		return this;
	}

	@Override
	protected cn.bran.japid.template.RenderResult getRenderResult() {
		return new cn.bran.japid.template.RenderResultPartial(getHeaders(), getOut(), this.renderingTime, this.actionRunners, this.sourceTemplate);
	}

}
