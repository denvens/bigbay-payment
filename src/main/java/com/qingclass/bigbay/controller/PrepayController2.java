package com.qingclass.bigbay.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Predicate;

import lombok.Data;

@RestController
public class PrepayController2 extends BaseController {

	@Data
	public static class Book {
		private String name;
		private Integer price;

		public Book(String name, int price) {
			this.name = name;
			this.price = price;
		}
	};

	public static void main(String[] args) {
		List<Book> books = Arrays.asList(new Book("Java从入门到精通", 55), new Book("深入理解Java虚拟机", 60),
				new Book("Java从入门到精通", 50), new Book("深入理解Java虚拟机", 65));
		
		List<Book> newDishList = books.stream().sorted(Comparator.comparing(Book::getPrice)).collect(Collectors.collectingAndThen(
				Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Book::getName))), ArrayList::new));
		newDishList.forEach(d -> System.out.println("price:" + d.getPrice() + ", name:" + d.getName()));
		
		System.out.println(newDishList);
	}

	static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
