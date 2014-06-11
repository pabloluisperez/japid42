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
package cn.bran.japid.util;

import java.util.HashMap;
import java.util.Map;


public class FlashScopeGeneric implements FlashScope {
	Map<String, Object> store = new HashMap<String, Object>();
	/* (non-Javadoc)
	 * @see bran.japid.Flash#hasSuccess()
	 */
	@Override
	public boolean hasSuccess() {
		return this.store.containsKey(SUCCESS);
	}

	/* (non-Javadoc)
	 * @see bran.japid.Flash#getSuccess()
	 */
	@Override
	public Object success() {
		return this.store.get(SUCCESS);
	}

	/* (non-Javadoc)
	 * @see bran.japid.Flash#putSuccess(java.lang.Object)
	 */
	public void putSuccess(Object message) {
		this.store.put(SUCCESS, message);
	}

	/* (non-Javadoc)
	 * @see bran.japid.Flash#hasError()
	 */
	@Override
	public boolean hasError() {
		return this.store.containsKey(ERROR);
	}
	
	/* (non-Javadoc)
	 * @see bran.japid.Flash#getError()
	 */
	@Override
	public Object error() {
		return this.store.get(ERROR);
	}

	/* (non-Javadoc)
	 * @see bran.japid.Flash#get(java.lang.String)
	 */
	@Override
	public Object get(String key) {
		return this.store.get(key);
	}

	@Override
	public boolean contains(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see cn.bran.japid.util.FlashScope#putSuccess(java.lang.String)
	 */
	@Override
	public void putSuccess(String message) {
		this.store.put(SUCCESS, message);
	}
}
