package org.sfa.request.mapper;

import org.mapstruct.Mapper;
import org.sfa.request.dto.CommentDTO;
import org.sfa.request.model.entity.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toEntity(CommentDTO commentDto);
    CommentDTO toDTO(Comment comment);


}
