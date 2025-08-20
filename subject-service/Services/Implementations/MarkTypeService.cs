using AutoMapper;
using subject_service.Common.Pagination;
using subject_service.Dtos.MarkType;
using subject_service.Models;
using subject_service.Repositories.Interfaces;
using subject_service.Services.Interfaces;

namespace subject_service.Services.Implementations
{
    public class MarkTypeService : IMarkTypeService
    {
        private readonly IMarkTypeRepository markTypeRepository;
        private readonly IMapper _mapper;

        public MarkTypeService(IMarkTypeRepository repository, IMapper mapper)
        {
            markTypeRepository = repository;
            _mapper = mapper;
        }

        public async Task<int> AddMarkType(MarkTypeCreateUpdateDto markTypeCreateDto)
        {
            var entity = _mapper.Map<MarkType>(markTypeCreateDto);
            return await markTypeRepository.AddAsync(entity);
        }

        public async Task<int> DeleteMarkType(long id)
        {
            return await markTypeRepository.DeleteAsync(id);
        }

        public async Task<(PagedResult<MarkTypeWithDeletedDto>, int)> GetAllMarkTypes(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await markTypeRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, true);
            var markTypeWithDeletedDtos = _mapper.Map<PagedResult<MarkTypeWithDeletedDto>>(result.Item1);
            return (markTypeWithDeletedDtos, result.Item2);
        }
        
        public async Task<(PagedResult<MarkTypeDto>, int)> GetAllExistingMarkTypes(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await markTypeRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, false);
            var markTypeDtos = _mapper.Map<PagedResult<MarkTypeDto>>(result.Item1);
            return (markTypeDtos, result.Item2);
        }

        public async Task<(MarkTypeDto, int)> GetMarkTypeById(long id)
        {
            var result = await markTypeRepository.GetByIdAsync(id);
            var markTypeDto = _mapper.Map<MarkTypeDto>(result);
            return (markTypeDto, result.Item2);
        }

        public async Task<int> UpdateMarkType(long id, MarkTypeCreateUpdateDto markTypeUpdateDto)
        {
            var entity = _mapper.Map<MarkType>(markTypeUpdateDto);
            entity.Id = id;
            return await markTypeRepository.UpdateAsync(entity);
        }
    }
}
