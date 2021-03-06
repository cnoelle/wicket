/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.model;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.apache.wicket.lambda.WicketBiFunction;
import org.apache.wicket.model.lambda.Address;
import org.apache.wicket.model.lambda.Person;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link IModel}'s methods
 */
public class IModelTest extends Assert
{
	private Person person;
	private final String name = "John";
	private final String street = "Strasse";

	@Before
	public void before()
	{
		person = new Person();
		person.setName(name);

		Address address = new Address();
		person.setAddress(address);
		address.setStreet(street);
		address.setNumber(123);
	}

	@Test
	public void filterMatch()
	{
		IModel<Person> johnModel = Model.of(person)
				.filter((p) -> p.getName().equals(name));

		assertThat(johnModel.getObject(), is(person));
	}

	@Test
	public void filterNoMatch()
	{
		IModel<Person> johnModel = Model.of(person)
				.filter((p) -> p.getName().equals("Jane"));

		assertThat(johnModel.getObject(), is(nullValue()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullFilter()
	{
		Model.of(person).filter(null);
	}

	@Test
	public void map()
	{
		IModel<String> personNameModel = Model.of(person).map(Person::getName);
		assertThat(personNameModel.getObject(), is(equalTo(name)));
	}

	@Test
	public void map2()
	{
		IModel<String> streetModel = Model.of(person).map(Person::getAddress).map(Address::getStreet);
		assertThat(streetModel.getObject(), is(equalTo(street)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullMapper()
	{
		Model.of(person).map(null);
	}

	@Test
	public void withMap()
	{
		IModel<String> janeModel = Model.of("Jane");
		WicketBiFunction<Person, String, String> function =
				(WicketBiFunction<Person, String, String>) (person1, other) ->
						person1.getName() + " is in relationship with " + other;
		IModel<String> relationShipModel = Model.of(person).mapWith(function, janeModel);
		assertThat(relationShipModel.getObject(), is(equalTo("John is in relationship with Jane")));
	}

	@Test
	public void withMapWithNullObject()
	{
		IModel<String> janeModel = Model.of((String)null);
		WicketBiFunction<Person, String, String> function =
				(WicketBiFunction<Person, String, String>) (person1, other) ->
						person1.getName() + " is in relationship with " + other;
		IModel<String> relationShipModel = Model.of(person).mapWith(function, janeModel);
		assertThat(relationShipModel.getObject(), is(nullValue()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void withMapWithNullModel()
	{
		IModel<String> janeModel = null;
		WicketBiFunction<Person, String, String> function =
				(WicketBiFunction<Person, String, String>) (person1, other) ->
						person1.getName() + " is in relationship with " + other;
		Model.of(person).mapWith(function, janeModel);
	}

	@Test(expected = IllegalArgumentException.class)
	public void withMapWithNullCombiner()
	{
		Model.of(person).mapWith(null, Model.of("Jane"));
	}

	@Test
	public void flatMap()
	{
		IModel<String> heirModel = Model.of(person)
			.flatMap(john ->
					LambdaModel.of(
						() -> john.getName() + " is my parent",
						john::setName
					)
			);
		assertThat(heirModel.getObject(), is(equalTo("John is my parent")));

		String newValue = "Matthias";
		heirModel.setObject(newValue);
		assertThat(heirModel.getObject(), is(equalTo("Matthias is my parent")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullFlatMapper()
	{
		Model.of(person).flatMap(null);
	}

	@Test
	public void orElse()
	{
		person.setName(null);
		String defaultName = "Default name";
		IModel<String> defaultNameModel = Model.of(person).map(Person::getName).orElse(defaultName);

		assertThat(defaultNameModel.getObject(), is(equalTo(defaultName)));
	}

	@Test
	public void orElseGet()
	{
		person.setName(null);
		String defaultName = "Default name";
		IModel<String> defaultNameModel = Model.of(person).map(Person::getName).orElseGet(() -> defaultName);

		assertThat(defaultNameModel.getObject(), is(equalTo(defaultName)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void orElseGetNullOther()
	{
		Model.of(person).map(Person::getName).orElseGet(null);
	}
}
