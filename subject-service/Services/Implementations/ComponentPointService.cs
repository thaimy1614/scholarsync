using AutoMapper;
using subject_service.Common.Pagination;
using subject_service.Dtos.ComponentPoint;
using subject_service.Models;
using subject_service.Repositories.Interfaces;
using subject_service.Services.Interfaces;

namespace subject_service.Services.Implementations
{
    public class ComponentPointService : IComponentPointService
    {
        private readonly IComponentPointRepository componentPointRepository;
        private readonly IMapper _mapper;
        private readonly HttpClient _httpClient;

        public ComponentPointService(IComponentPointRepository repository, IMapper mapper, HttpClient httpClient)
        {
            componentPointRepository = repository;
            _mapper = mapper;
            _httpClient = httpClient;
        }

        public Task<int> AddComponentPoint(ComponentPointCreateUpdateDto componentPointCreateDto)
        {
            var entity = _mapper.Map<ComponentPoint>(componentPointCreateDto);
            return componentPointRepository.AddAsync(entity);
        }

        public Task<int> DeleteComponentPoint(long id)
        {
            return componentPointRepository.DeleteAsync(id);
        }

        public async Task<(PagedResult<ComponentPointWithDeletedDto>, int)> GetAllComponentPoints(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await componentPointRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, true);
            var componentPointWithDeletedDtos = _mapper.Map<PagedResult<ComponentPointWithDeletedDto>>(result.Item1);
            return (componentPointWithDeletedDtos, result.Item2);
        }
        
        public async Task<(PagedResult<ComponentPointDto>, int)> GetAllExistingComponentPoints(int page, int pageSize, string sortColumn, bool isDescending)
        {
            var result = await componentPointRepository.GetAllAsync(page, pageSize, sortColumn, isDescending, false);
            var componentPointDtos = _mapper.Map<PagedResult<ComponentPointDto>>(result.Item1);
            return (componentPointDtos, result.Item2);
        }

        public async Task<(ComponentPointDto, int)> GetComponentPointById(long id)
        {
            var result = await componentPointRepository.GetByIdAsync(id);
            var componentPointDto = _mapper.Map<ComponentPointDto>(result.Item1);
            return (componentPointDto, result.Item2);
        }

        public Task<int> UpdateComponentPoint(long id, ComponentPointCreateUpdateDto componentPointUpdateDto)
        {
            var entity = _mapper.Map<ComponentPoint>(componentPointUpdateDto);
            entity.Id = id;
            return componentPointRepository.UpdateAsync(entity);
        }
    }
}
