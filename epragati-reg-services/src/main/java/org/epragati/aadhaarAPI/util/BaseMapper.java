package org.epragati.aadhaarAPI.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class BaseMapper<E, V> {

	public List<V> convertEntity(List<E> dtos) {

		return dtos.stream().map(e -> convertEntity(e)).collect(Collectors.toList());
	}

	public Optional<V> convertEntity(Optional<E> optionalDto) {

		if (optionalDto.isPresent()) {
			return Optional.of(convertEntity(optionalDto.get()));
		}
		return Optional.empty();
	}

	public abstract V convertEntity(E dto);

	public List<E> convertVO(List<V> vos) {

		return vos.stream().map(v -> convertVO(v)).collect(Collectors.toList());
	}

	// TODO: make as abstract
	public E convertVO(V vo) {
		throw new RuntimeException("Not implmenented");
	}
	
	public E convertVO(E e,V vo) {
		throw new RuntimeException("Not implmenented");
	}

	public Optional<E> convertVO(Optional<V> optionalVO) {

		if (optionalVO.isPresent()) {
			return Optional.of(convertVO(optionalVO.get()));
		}
		return Optional.empty();
	}
	
	public <T> void funPoint(T value, Consumer<T> consumer){
		if(value!=null){
			consumer.accept(value);
		}
	}
	
}
