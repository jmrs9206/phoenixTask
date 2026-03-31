import Link from "next/link";

type PaginationControlsProps = {
  page: number;
  totalPages: number;
  getHref: (page: number) => string;
};

export default function PaginationControls({ page, totalPages, getHref }: PaginationControlsProps) {
  if (totalPages <= 1) {
    return null;
  }

  const prevPage = page > 1 ? page - 1 : null;
  const nextPage = page < totalPages ? page + 1 : null;

  return (
    <div className="pagination">
      {prevPage ? (
        <Link className="pagination-link" href={getHref(prevPage)}>
          Previous
        </Link>
      ) : (
        <span className="pagination-link pagination-link--disabled">Previous</span>
      )}
      <span className="pagination-status">
        Page {page} of {totalPages}
      </span>
      {nextPage ? (
        <Link className="pagination-link" href={getHref(nextPage)}>
          Next
        </Link>
      ) : (
        <span className="pagination-link pagination-link--disabled">Next</span>
      )}
    </div>
  );
}
