/*
 * Copyright (C) 2014 Saúl Díaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sefford.kor.retrofit.interfaces;

import com.sefford.kor.errors.Error;
import com.sefford.kor.interactors.interfaces.NetworkDelegate;
import com.sefford.kor.responses.Response;

import retrofit.RetrofitError;

/**
 * Extension of Kor's Network Request to support Retrofit.
 *
 * @author Saul Diaz <sefford@gmail.com>
 */
public interface RetrofitRequest<R extends Response, E extends Error> extends NetworkDelegate<R, E> {

    /**
     * Generates a BaseError from a Retrofit error
     *
     * @param error Networking error
     * @return Composed error
     */
    E composeErrorResponse(RetrofitError error);

}
