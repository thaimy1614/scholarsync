using AutoMapper;

namespace logbook_service.Models.Mappings
{
    public class PagedResultConverter<TSource, TDestination> : ITypeConverter<PagedResult<TSource>, PagedResult<TDestination>>
    {
        private readonly IMapper _mapper;

        public PagedResultConverter(IMapper mapper)
        {
            _mapper = mapper;
        }

        public PagedResult<TDestination> Convert(PagedResult<TSource> source, PagedResult<TDestination> destination, ResolutionContext context)
        {
            var mappedItems = _mapper.Map<IEnumerable<TDestination>>(source.Items);

            return new PagedResult<TDestination>(mappedItems, source.TotalCount, source.CurrentPage, source.PageSize);
        }
    }
}
