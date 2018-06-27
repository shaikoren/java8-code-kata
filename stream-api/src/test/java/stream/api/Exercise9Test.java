package stream.api;

import common.test.tool.annotation.Difficult;
import common.test.tool.annotation.Easy;
import common.test.tool.dataset.ClassicOnlineStore;
import common.test.tool.entity.Customer;
import common.test.tool.util.CollectorImpl;
import org.junit.Test;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Exercise9Test extends ClassicOnlineStore {

    @Easy @Test
    public void simplestStringJoin() {
        List<Customer> customerList = this.mall.getCustomerList();

        /**
         * Implement a {@link Collector} which can create a String with comma separated names shown in the assertion.
         * The collector will be used by serial stream.
         */
        Supplier<Object> supplier = null;
        BiConsumer<Object, String> accumulator = null;
        BinaryOperator<Object> combiner = null;
        Function<Object, String> finisher = null;

        Collector<String, ?, String> toCsv =
            new CollectorImpl<>(supplier, accumulator, combiner, finisher, Collections.emptySet());
        String nameAsCsv = customerList.stream().map(Customer::getName).collect(toCsv);
        assertThat(nameAsCsv, is("Joe,Steven,Patrick,Diana,Chris,Kathy,Alice,Andrew,Martin,Amy"));
    }

    @Difficult @Test
    public void mapKeyedByItems() {
        List<Customer> customerList = this.mall.getCustomerList();

        /**
         * Implement a {@link Collector} which can create a {@link Map} with keys as item and
         * values as {@link Set} of customers who are wanting to buy that item.
         * The collector will be used by parallel stream.
         */
        Supplier<Object> supplier = null;
        BiConsumer<Object, Customer> accumulator = null;
        BinaryOperator<Object> combiner = null;
        Function<Object, Map<String, Set<String>>> finisher = null;

        Collector<Customer, ?, Map<String, Set<String>>> toItemAsKey =
            new CollectorImpl<>(supplier, accumulator, combiner, finisher, EnumSet.of(
                Collector.Characteristics.CONCURRENT,
                Collector.Characteristics.IDENTITY_FINISH));
        Map<String, Set<String>> itemMap = customerList.stream().parallel().collect(toItemAsKey);
        assertThat(itemMap.get("plane"), containsInAnyOrder("Chris"));
        assertThat(itemMap.get("onion"), containsInAnyOrder("Patrick", "Amy"));
        assertThat(itemMap.get("ice cream"), containsInAnyOrder("Patrick", "Steven"));
        assertThat(itemMap.get("earphone"), containsInAnyOrder("Steven"));
        assertThat(itemMap.get("plate"), containsInAnyOrder("Joe", "Martin"));
        assertThat(itemMap.get("fork"), containsInAnyOrder("Joe", "Martin"));
        assertThat(itemMap.get("cable"), containsInAnyOrder("Diana", "Steven"));
        assertThat(itemMap.get("desk"), containsInAnyOrder("Alice"));
    }

    @Difficult @Test
    public void bitList2BitString() {
        String bitList = "22-24,9,42-44,11,4,46,14-17,5,2,38-40,33,50,48";

        /**
         * Create a {@link String} of "n"th bit ON.
         * for example
         * "3" will be "001"
         * "1,3,5" will be "10101"
         * "1-3" will be "111"
         * "7,1-3,5" will be "1110101"
         */
        Collector<String, ?, String> toBitString = new Collector<String, List<Integer>, String>() {
            @Override public Supplier<List<Integer>> supplier() {
                return ArrayList::new;
            }

            @Override public BiConsumer<List<Integer>, String> accumulator() {
                return (list, str) -> {
                    List<String> splitString = Arrays.asList(str.split("-"));
                    List<Integer> splitInt = splitString.stream().map(Integer::valueOf).collect(Collectors.toList());
                    if (splitInt.size() > 1) {
                        list.addAll(Stream.iterate(splitInt.get(0), e -> ++e)
                                        .limit(splitInt.get(1) - splitInt.get(0) + 1)
                                        .collect(Collectors.toList()));
                    } else {
                        list.add(splitInt.get(0));
                    }
                };
            }

            @Override public BinaryOperator<List<Integer>> combiner() {
                return null;
            }

            @Override public Function<List<Integer>, String> finisher() {
                return list -> {
                    long max = list.stream().max(Comparator.naturalOrder()).get();
                    return list.stream().distinct().collect(
                        new Collector<Integer, List<String>, String>() {
                            @Override public Supplier<List<String>> supplier() {
                                return () -> Stream.generate(() -> "0")
                                    .limit(max)
                                    .collect(Collectors.toList());
                            }

                            @Override public BiConsumer<List<String>, Integer> accumulator() {
                                return (strList, nth) -> strList.set(nth - 1, "1");
                            }

                            @Override public BinaryOperator<List<String>> combiner() {
                                return null;
                            }

                            @Override public Function<List<String>, String> finisher() {
                                return strList -> strList.stream().collect(Collectors.joining());
                            }

                            @Override public Set<Characteristics> characteristics() {
                                return Collections.emptySet();
                            }
                        });
                };
            }

            @Override public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };

        String bitString = Arrays.stream(bitList.split(",")).collect(toBitString);
        assertThat(bitString, is("01011000101001111000011100000000100001110111010101")

        );
    }
}
